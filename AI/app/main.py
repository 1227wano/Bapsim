# app/main.py

# 개발중에만 사용할 환경변수 로드
from dotenv import load_dotenv
load_dotenv()

import os
from typing import Optional, List, Dict, Any
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import uvicorn
from openai import OpenAI

# === [추가] 표준 유틸 ===
import json, re, unicodedata, time
from pathlib import Path

# === [추가] RAG 로더 ===
import numpy as np
import faiss
from sentence_transformers import SentenceTransformer

# === [추가] SQL ===
from sqlalchemy import create_engine

# === [추가] 에이전트 툴 스펙/레지스트리 ===
from app.agents.tools import TOOLS_SPEC, TOOLS_EXEC, run_tool_safely

# -------------------------
# 환경변수/설정
# -------------------------
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_BASE_URL = os.getenv("OPENAI_BASE_URL")  # 선택: vLLM/ollama 등
OPENAI_MODEL = os.getenv("OPENAI_MODEL")        # 예: gpt-4o-mini
APP_ENV = os.getenv("APP_ENV")

# RAG 관련 (환경변수로 덮어쓰기 가능)
INDEX_DIR = Path(os.getenv("RAG_INDEX_DIR", "./out/index-v1"))
INDEX_PATH = INDEX_DIR / "faiss.index"
META_PATH  = INDEX_DIR / "meta.json"
EMBED_MODEL_NAME = os.getenv("EMBED_MODEL_NAME", "intfloat/multilingual-e5-base")
TOP_K = int(os.getenv("RAG_TOP_K", "6"))
MAX_CONTEXT_CHARS = int(os.getenv("RAG_MAX_CONTEXT_CHARS", "2400"))

# SQL 관련
DB_URL = os.getenv("DB_URL")  # 예: mysql+pymysql://campus_ro:***@db:3306/campus
ALLOW_TABLES = [t.strip() for t in os.getenv("ALLOW_TABLES","").split(",") if t.strip()]
SQL_MAX_LIMIT = int(os.getenv("SQL_MAX_LIMIT","200"))
SQL_MAX_ROWS  = int(os.getenv("SQL_MAX_ROWS","200"))

# -------------------------
# 요청/응답 스키마
# -------------------------
class ChatMessage(BaseModel):
    role: str = Field(..., description="system|user|assistant")
    content: str

class ChatRequest(BaseModel):
    user_id: str
    message: str
    context: Optional[Dict[str, Any]] = None
    history: Optional[List[ChatMessage]] = None
    language: Optional[str] = None          # ← 프론트/백엔드가 감지해서 넣어줌
    locale_hint: Optional[str] = None       # 선택
    culture_hint: Optional[str] = None      # 선택

class ChatResponse(BaseModel):
    reply: str
    model: str
    usage: Optional[Dict[str, Any]] = None
    meta: Dict[str, Any] = Field(default_factory=dict)

# -------------------------
# FastAPI
# -------------------------
app = FastAPI(title="Campus Chatbot API", version="0.2.0")
app.add_middleware(
    CORSMiddleware,
    allow_origins=os.getenv("CORS_ALLOW_ORIGINS", "*").split(","),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# -------------------------
# [추가] RAG 유틸 (싱글톤 로딩)
# -------------------------
_model = None
_index = None
_chunks: List[str] = []
_metas: List[Dict[str, Any]] = []

def _normalize(t: str) -> str:
    t = unicodedata.normalize("NFKC", t or "")
    return re.sub(r"\s+", " ", t).strip()

def _ensure_loaded():
    """앱 생명주기 동안 한 번만 모델/인덱스 로드"""
    global _model, _index, _chunks, _metas
    if _model is None:
        _model = SentenceTransformer(EMBED_MODEL_NAME)
    if _index is None:
        if not INDEX_PATH.exists() or not META_PATH.exists():
            raise RuntimeError(f"RAG index not found at {INDEX_DIR}")
        _index = faiss.read_index(str(INDEX_PATH))
    if not _chunks or not _metas:
        data = json.loads(META_PATH.read_text(encoding="utf-8"))
        _chunks, _metas = data["chunks"], data["meta"]

def _embed_query(q: str) -> np.ndarray:
    # e5는 query에 프리픽스 권장
    v = _model.encode([f"query: {_normalize(q)}"], normalize_embeddings=True, show_progress_bar=False)
    return np.asarray(v, dtype="float32")

def search_topk(question: str, top_k: int = TOP_K) -> List[Dict[str, Any]]:
    _ensure_loaded()
    q = _embed_query(question)
    scores, idxs = _index.search(q, top_k)
    idxs, sims = idxs[0], scores[0]
    hits: List[Dict[str, Any]] = []
    for i, s in zip(idxs, sims):
        if i == -1:
            continue
        hits.append({"text": _chunks[i], "score": float(s), "meta": _metas[i]})
    return hits

def format_context(hits: List[Dict[str, Any]], limit: int = MAX_CONTEXT_CHARS) -> str:
    buf, used = [], 0
    for h in hits:
        title = h["meta"].get("title") or h["meta"].get("doc_id") or "doc"
        cid = h["meta"].get("chunk_id", 0)
        block = f"[{title} #chunk{cid}]\n{h['text']}\n"
        if used + len(block) > limit:
            break
        buf.append(block); used += len(block)
    return "\n---\n".join(buf)

# -------------------------
# 앱 시작 시 준비
# -------------------------
@app.on_event("startup")
def _warmup():
    # RAG 인덱스
    try:
        _ensure_loaded()
    except Exception as e:
        print(f"[RAG warmup] skip or warn: {e}")

    # DB 엔진
    try:
        if DB_URL:
            app.state.engine = create_engine(DB_URL, pool_pre_ping=True, pool_recycle=3600)
            print("[DB] engine ready")
        else:
            app.state.engine = None
            print("[DB] DB_URL not set -> SQL tool disabled")
    except Exception as e:
        app.state.engine = None
        print(f"[DB] engine init failed: {e}")

@app.get("/healthz")
async def healthz():
    return {
        "status": "ok",
        "env": APP_ENV,
        "rag_index_ready": INDEX_PATH.exists(),
        "db_ready": bool(getattr(app.state, "engine", None))
    }

# -------------------------
# LLM 헬퍼/에이전트 루프
# -------------------------
def _make_openai_client() -> OpenAI:
    if OPENAI_BASE_URL:
        return OpenAI(api_key=OPENAI_API_KEY, base_url=OPENAI_BASE_URL)
    return OpenAI(api_key=OPENAI_API_KEY)

def _llm_sql_generator(client: OpenAI, prompt: str) -> str:
    """SQL 생성용 미니 헬퍼(SELECT 1개만)."""
    mdl = os.getenv("SQL_LLM_MODEL", OPENAI_MODEL or "gpt-4o-mini")
    r = client.chat.completions.create(
        model=mdl,
        messages=[
            {"role": "system", "content": "You generate a single SELECT SQL only."},
            {"role": "user", "content": prompt},
        ],
        temperature=0.0,
        max_tokens=300,
    )
    return r.choices[0].message.content.strip()

async def run_agent_loop(client: OpenAI, base_messages: List[Dict[str, str]], ctx: Dict[str, Any], max_steps: int = 2):
    """
    OpenAI function-calling 기반 초경량 에이전트 루프(최대 2스텝).
    tools.py의 lang_detect는 제거되었다고 가정.
    """
    messages = list(base_messages)
    tools_used, tool_results = [], []
    for _ in range(max_steps):
        resp = client.chat.completions.create(
            model=OPENAI_MODEL,
            messages=messages,
            tools=TOOLS_SPEC,       # offtopic/sql/rag/clarify/pii 만 포함되어야 함
            tool_choice="auto",
            temperature=0.2,
            max_tokens=800,
        )
        msg = resp.choices[0].message
        calls = getattr(msg, "tool_calls", None)

        if not calls:  # 최종 답변
            return msg.content, getattr(resp, "usage", None), tools_used, tool_results

        # 각 tool 호출 실행 → 결과 message에 붙이기
        for call in calls:
            name = call.function.name
            args = json.loads(call.function.arguments or "{}")
            result = run_tool_safely(name, args, ctx)
            tools_used.append(name)
            # 디버그 프리뷰(키 이름만 가볍게)
            tool_results.append({"name": name, "keys": list(result.keys())[:8]})
            messages.append({
                "role": "tool",
                "tool_call_id": call.id,
                "name": name,
                "content": json.dumps(result, ensure_ascii=False),
            })

    # 스텝 초과 → 마지막 응답 생성 시도
    final = client.chat.completions.create(
        model=OPENAI_MODEL, messages=messages, temperature=0.2, max_tokens=800
    )
    return final.choices[0].message.content, getattr(final, "usage", None), tools_used, tool_results

# -------------------------
# Chat 엔드포인트
# -------------------------
@app.post("/chat", response_model=ChatResponse)
async def chat(payload: ChatRequest):
    t0 = time.time()
    client = _make_openai_client()

    # 언어는 프론트/다른 백엔드가 확정해서 보낸 값을 신뢰
    lang = (payload.language or "ko").strip()

    # 1) RAG 컨텍스트(있으면 활용)
    context_text = ""
    rag_sources = []
    try:
        hits = search_topk(payload.message, TOP_K)
        context_text = format_context(hits, MAX_CONTEXT_CHARS) if hits else ""
        rag_sources = [
            {"title": h["meta"].get("title"), "chunk_id": h["meta"].get("chunk_id"), "score": h["score"]}
            for h in hits
        ]
    except Exception:
        # 인덱스 없거나 로딩 실패해도 대화는 진행
        pass

    # 2) 프롬프트 구성
    system_prompt = (
        "You are a multilingual campus assistant for '헤이영 캠퍼스'. "
        f"Always respond STRICTLY in '{lang}'. "
        "Use the provided context and available tools when helpful. "
        "If the answer is not in the context or DB, say you don't know. "
        "Always mask PII and ask at most one concise clarifying question."
    )
    if payload.culture_hint:
        system_prompt += f" Cultural guidance: {payload.culture_hint} "

    user_prompt = (
        f"[lang={lang}] {payload.message}\n\n"
        f"---\nContext:\n{context_text if context_text else '(no context)'}"
    )

    messages = [{"role": "system", "content": system_prompt}]
    if payload.history:
        messages.extend({"role": m.role, "content": m.content} for m in payload.history)
    messages.append({"role": "user", "content": user_prompt})

    # 3) 도구 컨텍스트 주입(ctx)
    ctx: Dict[str, Any] = {
        # SQL
        "engine": getattr(app.state, "engine", None),
        "llm_sql": (lambda p: _llm_sql_generator(client, p)),
        "allow_tables": ALLOW_TABLES,
        "sql_max_limit": SQL_MAX_LIMIT,
        "sql_max_rows": SQL_MAX_ROWS,
        # RAG
        "rag_search": lambda q, k, f: search_topk(q, k),
        "rag_max_chars": MAX_CONTEXT_CHARS,
    }

    # 4) 에이전트 1~2스텝 실행 → 실패 시 폴백
    try:
        reply_text, usage, tools_used, tool_results = await run_agent_loop(client, messages, ctx, max_steps=2)
    except Exception as e:
        # 폴백: 기존 단발 호출
        try:
            completion = client.chat.completions.create(
                model=OPENAI_MODEL, messages=messages, temperature=0.2, top_p=1.0, max_tokens=800,
            )
            reply_text = completion.choices[0].message.content
            usage = getattr(completion, "usage", None)
        except Exception as e2:
            raise HTTPException(status_code=500, detail=f"LLM request failed: {e} / fallback: {e2}")
        tools_used, tool_results = [], []

    # 5) 응답
    latency_ms = int((time.time()-t0)*1000)
    return ChatResponse(
        reply=reply_text,
        model=OPENAI_MODEL,
        usage=(usage.model_dump() if hasattr(usage, "model_dump") else (dict(usage) if usage else None)),
        meta={
            "user_id": payload.user_id,
            "language": lang,
            "rag_used": bool(context_text),
            "rag_sources": rag_sources,      # 프론트 출처 노출용
            "tools_used": tools_used,        # 디버그/시연용
            "tool_results_preview": tool_results,
            "latency_ms": latency_ms,
        },
    )

if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT", "8000")),
        reload=True,
    )
