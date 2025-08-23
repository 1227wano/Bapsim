# app/main.py
import os
from typing import Optional, List, Dict, Any
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import uvicorn
from openai import OpenAI

# === [추가] RAG 로더 ===
import json, re, unicodedata
from pathlib import Path
import numpy as np
import faiss
from sentence_transformers import SentenceTransformer

# -------------------------
# 환경변수/설정
# -------------------------
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_MODEL = os.getenv("OPENAI_MODEL")
APP_ENV = os.getenv("APP_ENV")

# RAG 관련 (환경변수로 덮어쓰기 가능)
INDEX_DIR = Path(os.getenv("RAG_INDEX_DIR", "./out/index-v1"))
INDEX_PATH = INDEX_DIR / "faiss.index"
META_PATH  = INDEX_DIR / "meta.json"
EMBED_MODEL_NAME = os.getenv("EMBED_MODEL_NAME", "intfloat/multilingual-e5-base")
TOP_K = int(os.getenv("RAG_TOP_K", "6"))
MAX_CONTEXT_CHARS = int(os.getenv("RAG_MAX_CONTEXT_CHARS", "2400"))

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
    language: Optional[str] = None

class ChatResponse(BaseModel):
    reply: str
    model: str
    usage: Optional[Dict[str, Any]] = None
    meta: Dict[str, Any] = Field(default_factory=dict)

# -------------------------
# FastAPI
# -------------------------
app = FastAPI(title="Campus Chatbot API", version="0.1.0")
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
    t = unicodedata.normalize("NFKC", t)
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
    # e5는 query에 프리픽스 필수
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

# 앱 시작 시 미리 로드(선택)
@app.on_event("startup")
def _warmup():
    try:
        _ensure_loaded()
    except Exception as e:
        # 인덱스가 아직 없을 수 있으니 경고만
        print(f"[RAG warmup] skip or warn: {e}")

@app.get("/healthz")
async def healthz():
    return {"status": "ok", "env": APP_ENV, "rag_index_ready": INDEX_PATH.exists()}

# -------------------------
# Chat 엔드포인트
# -------------------------
@app.post("/chat", response_model=ChatResponse)
async def chat(payload: ChatRequest):
    client = OpenAI(api_key=OPENAI_API_KEY)

    # 1) RAG 컨텍스트 구성 (인덱스가 있을 때만)
    context_text = ""
    rag_sources = []
    try:
        hits = search_topk(payload.message, TOP_K)
        context_text = format_context(hits, MAX_CONTEXT_CHARS) if hits else ""
        rag_sources = [
            {"title": h["meta"].get("title"), "chunk_id": h["meta"].get("chunk_id"), "score": h["score"]}
            for h in hits
        ]
    except Exception as _:
        # 인덱스 없거나 로딩 실패해도 대화는 진행
        pass

    # 2) 프롬프트 구성
    system_prompt = (
        "You are a multilingual campus assistant for '헤이영 캠퍼스'. "
        "Use ONLY the provided context to answer. If the answer is not in the context, say you don't know. "
        f"Respond in the requested language."
    )

    user_prompt = (
        f"[lang={payload.language}] {payload.message}\n\n"
        f"---\nContext:\n{context_text if context_text else '(no context)'}"
    )

    messages = [{"role": "system", "content": system_prompt}]
    if payload.history:
        messages.extend({"role": m.role, "content": m.content} for m in payload.history)
    messages.append({"role": "user", "content": user_prompt})

    # 3) LLM 호출
    try:
        completion = client.chat.completions.create(
            model=OPENAI_MODEL,
            messages=messages,
            temperature=0.2,
            top_p=1.0,
            max_tokens=800,
        )
        reply_text = completion.choices[0].message.content
        u = getattr(completion, "usage", None)
        usage: Optional[Dict[str, Any]] = (
            u.model_dump() if hasattr(u, "model_dump") else (dict(u) if u else None)
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"LLM request failed: {e}")

    return ChatResponse(
        reply=reply_text,
        model=OPENAI_MODEL,
        usage=usage,
        meta={
            "user_id": payload.user_id,
            "language": payload.language,
            "rag_used": bool(context_text),
            "rag_sources": rag_sources,  # 프론트에서 출처 노출 가능
        },
    )

if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT", "8000")),
        reload=True,
    )
