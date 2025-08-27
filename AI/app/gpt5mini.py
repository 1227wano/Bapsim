# app/main.py

# 개발중에만 사용할 환경변수 로드
# 배포/운영시에는 컨테이너 실행시 환경변수 직접 로드할 것
from dotenv import load_dotenv
load_dotenv()

import os
from typing import Optional, List, Dict, Any
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import uvicorn
from openai import OpenAI

# === 표준 유틸 ===
import json, re, unicodedata, time
from pathlib import Path

# === RAG 로더 ===
import numpy as np
import faiss
from sentence_transformers import SentenceTransformer

# === SQL ===
from sqlalchemy import create_engine

# === 에이전트 툴 스펙/레지스트리 ===
from app.agents.tools import TOOLS_SPEC, run_tool_safely

# -------------------------
# 환경변수/설정
# -------------------------
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-5-mini")  # 기본값 gpt-5-mini
APP_ENV = os.getenv("APP_ENV")  # 개발/배포 환경 구분

# GPT-5 튜닝 옵션(환경변수로 조절 가능)
REASONING_EFFORT = os.getenv("REASONING_EFFORT", "medium")  # minimal|low|medium|high
VERBOSITY = os.getenv("VERBOSITY", "medium")                # low|medium|high
ALLOWED_TOOLS = [t.strip() for t in os.getenv("ALLOWED_TOOLS","").split(",") if t.strip()]

# RAG 인덱스 파일 위치
# 인덱스 파일 저장 경로. 서버 실행 경로에 따라 조정 필요
INDEX_DIR = Path(os.getenv("RAG_INDEX_DIR", "./out/index-v1"))
INDEX_PATH = INDEX_DIR / "faiss.index"
META_PATH = INDEX_DIR / "meta.json"
EMBED_MODEL_NAME = os.getenv("EMBED_MODEL_NAME", "intfloat/multilingual-e5-base")
TOP_K = int(os.getenv("RAG_TOP_K", "6"))  # 검색 후 가져올 결과 수
MAX_CONTEXT_CHARS = int(os.getenv("RAG_MAX_CONTEXT_CHARS", "2000"))  # llm에 전달할 context 최대 글자수

# SQL 관련
DB_URL = os.getenv("DB_URL")  # 예: mysql+pymysql://campus_ro:***@db:3306/campus
ALLOW_TABLES = [t.strip() for t in os.getenv("ALLOW_TABLES", "").split(",") if t.strip()]
SQL_MAX_LIMIT = int(os.getenv("SQL_MAX_LIMIT", "200"))
SQL_MAX_ROWS = int(os.getenv("SQL_MAX_ROWS", "200"))

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
    language: Optional[str] = None  # ← 백엔드에서 감지해서 넣어줌


class ChatResponse(BaseModel):
    reply: str
    model: str
    usage: Optional[Dict[str, Any]] = None  # OPENAI에서 주는 토큰 사용량 등 usage 데이터
    meta: Dict[str, Any] = Field(default_factory=dict)  # 로깅/디버깅용 데이터


# -------------------------
# FastAPI 앱 및 CORS 설정
# -------------------------
app = FastAPI()
app.add_middleware(
    CORSMiddleware,
    allow_origins=os.getenv("CORS_ALLOW_ORIGINS", "*").split(","),  # 쉼표로 여러 origin 허용
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# -------------------------
# RAG 유틸 (싱글톤 로딩)
# -------------------------
_model = None  # SentenceTransformer 인스턴스
_index = None  # FAISS index
_chunks: List[str] = []
_metas: List[Dict[str, Any]] = []


def _normalize(t: str) -> str:
    """NFKC 인코딩 + 중복공백 제거 정규화"""
    t = unicodedata.normalize("NFKC", t or "")
    return re.sub(r"\s+", " ", t).strip()


def _ensure_loaded():
    """앱 생명주기 동안 한 번만 모델/인덱스 로드"""
    global _model, _index, _chunks, _metas
    if _model is None:
        _model = SentenceTransformer(EMBED_MODEL_NAME)
    if _index is None:
        # 인덱스가 없으면 RAG 없이 동작하도록 예외 처리
        if not INDEX_PATH.exists() or not META_PATH.exists():
            raise RuntimeError(f"RAG index not found at {INDEX_DIR}")
        _index = faiss.read_index(str(INDEX_PATH))
    if not _chunks or not _metas:
        data = json.loads(META_PATH.read_text(encoding="utf-8"))
        _chunks, _metas = data["chunks"], data["meta"]


def _embed_query(q: str) -> np.ndarray:
    """query 임베딩 생성"""
    v = _model.encode(
        [f"query: {_normalize(q)}"],
        normalize_embeddings=True,
        show_progress_bar=False,
    )
    return np.asarray(v, dtype="float32")


def search_topk(question: str, top_k: int = TOP_K) -> List[Dict[str, Any]]:
    """질문 문장을 임베딩하여 FAISS에서 top_k개 검색
    chunk 결과 및 score값 반환"""
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
    """top_k개의 검색결과를 LLM에 넣기 좋은 형식으로 변환
    문서 제목과 각 chunk의 id를 메타데이터에 담음"""
    buf, used = [], 0
    for h in hits:
        title = h["meta"].get("title") or h["meta"].get("doc_id") or "doc"
        cid = h["meta"].get("chunk_id", 0)
        block = f"[{title} #chunk{cid}]\n{h['text']}\n"
        # MAX_CONTEXT_CHAR를 넘지 않도록 자름
        if used + len(block) > limit:
            break
        buf.append(block)
        used += len(block)
    return "\n---\n".join(buf)


# -------------------------
# 앱 시작 시 준비
# -------------------------
@app.on_event("startup")
def _warmup():
    # RAG 인덱스 준비
    # 인덱스 파일이 없는 등 오류 발생시에도 일단 서비스는 실행
    try:
        _ensure_loaded()
        print("[RAG] index ready")
    except Exception as e:
        print(f"[RAG warmup] skip or warn: {e}")

    # DB 엔진 준비
    # DB서버 미작동 등 오류 발생시에도 일단 서비스는 실행
    try:
        if DB_URL:
            app.state.engine = create_engine(
                DB_URL, pool_pre_ping=True, pool_recycle=3600
            )
            print("[DB] engine ready")
        else:
            app.state.engine = None
            print("[DB] DB_URL not set -> SQL tool disabled")
    except Exception as e:
        app.state.engine = None
        print(f"[DB] engine init failed: {e}")

    # OPENAI API key 로드
    app.state.openai = OpenAI(api_key=OPENAI_API_KEY)


# healthcheck
# RAG 인덱스와 DB 준비상태 확인
@app.get("/healthz")
async def healthz():
    return {
        "status": "ok",
        "env": APP_ENV,
        "rag_index_ready": INDEX_PATH.exists(),
        "db_ready": bool(getattr(app.state, "engine", None)),
    }


# -------------------------
# LLM 헬퍼/에이전트 루프 (Responses API)
# -------------------------
async def run_agent_loop(
    client: OpenAI,
    base_messages: List[Dict[str, str]],
    ctx: Dict[str, Any],
    max_steps: int = 3,
):
    """
    Responses API 기반 에이전트 루프(최대 3스텝).
    tools=TOOLS_SPEC 를 전달하고, 모델이 tool_call을 내면 실행 후 tool 메시지를 이어붙여 재호출합니다.
    """
    messages = list(base_messages)
    tools_used, tool_results = [], []
    llm_ms = 0
    llm_calls = 0

    tool_choice = (
        {"type": "allowed_tools", "mode": "auto",
         "tools": [{"type": "function", "name": n} for n in ALLOWED_TOOLS]}
        if ALLOWED_TOOLS else "auto"
    )

    for _ in range(max_steps):
        _t = time.perf_counter()
        resp = client.responses.create(
            model=OPENAI_MODEL,
            input=messages,  # chat 메시지 배열 그대로 전달 가능
            tools=TOOLS_SPEC,  # 기존 function tool 스펙 그대로 사용
            tool_choice=tool_choice,  # 필요시 auto/allowed_tools/required로 제약 가능
            max_output_tokens=800,
            reasoning={"effort": REASONING_EFFORT},
            text={"verbosity": VERBOSITY},
        )
        llm_ms += int((time.perf_counter() - _t) * 1000)
        llm_calls += 1

        # 1) 최종 답변 텍스트
        out_text = getattr(resp, "output_text", None)
        tool_calls = []

        # 2) tool call 추출(Responses API는 output 리스트에 tool_call 아이템이 섞여 올 수 있음)
        for item in getattr(resp, "output", []) or []:
            if getattr(item, "type", None) == "tool_call":
                tool_calls.append(item)

        # tool call이 없으면 최종 답변 반환
        if not tool_calls and out_text:
            return (
                out_text,
                getattr(resp, "usage", None),
                tools_used,
                tool_results,
                llm_ms,
                llm_calls,
            )

        # 각 tool 호출 실행 → 결과 message에 붙이기
        for tc in tool_calls:
            name = getattr(tc, "name", None)
            raw_args = getattr(tc, "arguments", {}) or {}
            try:
                args = raw_args if isinstance(raw_args, dict) else json.loads(raw_args)
            except Exception:
                args = {}
            result = run_tool_safely(name, args, ctx)
            tools_used.append(name)
            # 디버그 프리뷰(키 이름만 가볍게)
            tool_results.append({"name": name, "keys": list(result.keys())[:8]})
            messages.append(
                {
                    "role": "tool",
                    "tool_call_id": getattr(tc, "id", None),
                    "name": name,
                    "content": json.dumps(result, ensure_ascii=False),
                }
            )

    # 스텝 초과 → 마지막 응답 생성 시도
    _t = time.perf_counter()
    final = client.responses.create(
        model=OPENAI_MODEL,
        input=messages,
        max_output_tokens=800,
        tool_choice="none",
        reasoning={"effort": REASONING_EFFORT},
        text={"verbosity": VERBOSITY},

    )
    llm_ms += int((time.perf_counter() - _t) * 1000)
    llm_calls += 1

    return (
        final.output_text,
        getattr(final, "usage", None),
        tools_used,
        tool_results,
        llm_ms,
        llm_calls,
    )


# -------------------------
# Chat 엔드포인트
# -------------------------
@app.post("/chat", response_model=ChatResponse)
async def chat(payload: ChatRequest):
    """
    단일 대화 처리 파이프라인
      1) 가능하면 RAG로 문맥 검색 후 컨텍스트 문자열 구성(payload.language에 따른 문자열 추가도 구현 예정)
      2) 시스템/유저 프롬프트 조립
      3) 에이전트 루프 실행(최대 3스텝), 실패 시 단발 LLM 호출로 fallback
      4) 응답 + 메타데이터(지연/툴/출처/사용량 등) 반환
    """
    t0 = time.perf_counter()  # 총 지연시간
    client = getattr(app.state, "openai", None) or OpenAI(api_key=OPENAI_API_KEY)

    # 언어는 프론트/다른 백엔드가 확정해서 보낸 값을 신뢰
    # 주요 언어가 아닌 것들은 따로 프롬프트 추가하지 않고 LLM 자체 기능에 맡김
    # 식문화 관련해서 context에 추가할 내용이 있는 언어는 프롬프트 추가하는 코드 구현 예정
    lang = (payload.language or "ko").strip()

    # 1) RAG 컨텍스트(있으면 활용)
    context_text = ""
    rag_sources: List[Dict[str, Any]] = []
    try:
        hits = search_topk(payload.message, TOP_K)
        context_text = format_context(hits, MAX_CONTEXT_CHARS) if hits else ""
        rag_sources = [
            {
                "title": h["meta"].get("title"),
                "chunk_id": h["meta"].get("chunk_id"),
                "score": h["score"],
            }
            for h in hits
        ]
    except Exception:
        # 인덱스 없거나 로딩 실패해도 대화는 진행
        pass

    # 2) DB schemas
    schema_hints = '''
CREATE TABLE `ai_service` (
  `session_id` int NOT NULL AUTO_INCREMENT,
  `assistant_message` varchar(500) DEFAULT NULL,
  `language` varchar(5) DEFAULT NULL,
  `llm_latency_ms` int DEFAULT NULL,
  `rag_used` bit(1) DEFAULT NULL,
  `total_latency_ms` int DEFAULT NULL,
  `usage_completion_token` int DEFAULT NULL,
  `usage_prompt_token` int DEFAULT NULL,
  `usage_total_tokens` int DEFAULT NULL,
  `user_message` varchar(500) NOT NULL,
  `user_no` bigint NOT NULL,
  PRIMARY KEY (`session_id`),
  KEY `FKkuddcm60ngkv7nwgh4jq06byw` (`user_no`),
  CONSTRAINT `FKkuddcm60ngkv7nwgh4jq06byw` FOREIGN KEY (`user_no`) REFERENCES `member` (`user_no`)
);

CREATE TABLE `cafeterias` (
  `cafe_no` bigint NOT NULL AUTO_INCREMENT,
  `build_name` varchar(100) NOT NULL,
  `close_time` datetime(6) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_id` varchar(100) NOT NULL,
  `del_yn` varchar(1) NOT NULL,
  `open_time` datetime(6) NOT NULL,
  `phone_no` bigint NOT NULL,
  `run_yn` varchar(1) NOT NULL,
  `uni_id` int NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `updated_id` varchar(100) NOT NULL,
  `visitor` bigint NOT NULL,
  PRIMARY KEY (`cafe_no`),
  KEY `FK45m3c5v2k3lgru6pf5qi16fsd` (`uni_id`),
  CONSTRAINT `FK45m3c5v2k3lgru6pf5qi16fsd` FOREIGN KEY (`uni_id`) REFERENCES `university` (`uni_id`)
);

CREATE TABLE `food` (
  `menu_id` varchar(100) NOT NULL,
  `allergy` bigint NOT NULL,
  `allergy_info` varchar(500) DEFAULT NULL,
  `category` varchar(10) NOT NULL,
  `content` varchar(500) DEFAULT NULL,
  `kcal` bigint NOT NULL,
  `menu_name` varchar(100) NOT NULL,
  `photo_path` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`menu_id`)
);

CREATE TABLE `member` (
  `user_no` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_id` varchar(100) NOT NULL,
  `uni_id` int NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `updated_id` varchar(100) NOT NULL,
  `user_email` varchar(100) DEFAULT NULL,
  `user_id` varchar(100) NOT NULL,
  `user_name` varchar(100) NOT NULL,
  `user_pass` varchar(500) NOT NULL,
  `user_phone` varchar(20) DEFAULT NULL,
  `user_status` varchar(20) NOT NULL,
  `user_type` varchar(20) NOT NULL,
  PRIMARY KEY (`user_no`),
  KEY `FK9q5d146oedfylqq10ynsw2rad` (`uni_id`),
  CONSTRAINT `FK9q5d146oedfylqq10ynsw2rad` FOREIGN KEY (`uni_id`) REFERENCES `university` (`uni_id`)
);

CREATE TABLE `menu_price` (
  `price_no` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `created_id` varchar(100) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `effective_date` date NOT NULL,
  `expiry_date` date DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `kind` varchar(1) NOT NULL,
  `meal_type` varchar(100) NOT NULL,
  `price` bigint NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `updated_id` varchar(100) NOT NULL,
  PRIMARY KEY (`price_no`)
);

CREATE TABLE `menus` (
  `menu_no` bigint NOT NULL AUTO_INCREMENT,
  `cafe_no` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_id` varchar(100) NOT NULL,
  `is_signature` bit(1) NOT NULL,
  `kind` varchar(1) NOT NULL,
  `meal_type` varchar(100) NOT NULL,
  `menu_date` date DEFAULT NULL,
  `menu_id` varchar(100) NOT NULL,
  `res_no` bigint DEFAULT NULL,
  `sold_out` bit(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `updated_id` varchar(100) NOT NULL,
  PRIMARY KEY (`menu_no`),
  KEY `FKnykch7bxg7bj2jild7whax77q` (`cafe_no`),
  KEY `FKddapuc09wj3277ktku7kvsogc` (`menu_id`),
  KEY `FKnnvxcvyycmv9i1wk0xa5jgqm7` (`res_no`),
  CONSTRAINT `FKddapuc09wj3277ktku7kvsogc` FOREIGN KEY (`menu_id`) REFERENCES `food` (`menu_id`),
  CONSTRAINT `FKnnvxcvyycmv9i1wk0xa5jgqm7` FOREIGN KEY (`res_no`) REFERENCES `restaurants` (`res_no`),
  CONSTRAINT `FKnykch7bxg7bj2jild7whax77q` FOREIGN KEY (`cafe_no`) REFERENCES `cafeterias` (`cafe_no`)
);

CREATE TABLE `payment` (
  `payment_id` bigint NOT NULL AUTO_INCREMENT,
  `amount` int NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `transaction_id` varchar(100) NOT NULL,
  `user_no` bigint NOT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `FKq00swcd16wo3krqt988obcrpf` (`user_no`),
  CONSTRAINT `FKq00swcd16wo3krqt988obcrpf` FOREIGN KEY (`user_no`) REFERENCES `member` (`user_no`)
);

CREATE TABLE `point_history` (
  `point_id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `payment_id` bigint DEFAULT NULL,
  `point_changed` int DEFAULT NULL,
  `reason` varchar(50) DEFAULT NULL,
  `user_no` bigint NOT NULL,
  PRIMARY KEY (`point_id`),
  KEY `FKkq2exxev3tsehlgb2eg06csb3` (`payment_id`),
  KEY `FKa8d7aw32854b8cye192tnd3t5` (`user_no`),
  CONSTRAINT `FKa8d7aw32854b8cye192tnd3t5` FOREIGN KEY (`user_no`) REFERENCES `member` (`user_no`),
  CONSTRAINT `FKkq2exxev3tsehlgb2eg06csb3` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`payment_id`)
);

CREATE TABLE `price` (
  `menu_no` bigint NOT NULL,
  `price` bigint NOT NULL,
  PRIMARY KEY (`menu_no`)
);

CREATE TABLE `restaurants` (
  `res_no` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(2000) NOT NULL,
  `close_time` datetime(6) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `created_id` varchar(100) NOT NULL,
  `del_yn` varchar(1) NOT NULL,
  `open_time` datetime(6) NOT NULL,
  `phone_no` bigint NOT NULL,
  `res_name` varchar(100) NOT NULL,
  `run_yn` varchar(1) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `updated_id` varchar(100) NOT NULL,
  `visitor` bigint NOT NULL,
  PRIMARY KEY (`res_no`)
);

CREATE TABLE `university` (
  `uni_id` int NOT NULL AUTO_INCREMENT,
  `uni_name` varchar(30) NOT NULL,
  PRIMARY KEY (`uni_id`)
);
'''

    # 3) 프롬프트 구성
    system_prompt = (
        "You are the campus assistant for '헤이영 캠퍼스'.\n"
        "Tool policy:\n"
        "- Use `sql_answer` for menus, prices, cafeterias, hours, or anything in the DB schema.\n"
        "- Use `rag_lookup` for policies, events, user guides, or anything not in the DB.\n"
        "- If neither can answer, say you don't know.\n"
        f"- Always answer strictly in the user's language: '{lang}'.\n"
        "\n"
        "Output rules (IMPORTANT):\n"
        "1) Present ONLY the final result the user asked for.\n"
        "2) DO NOT mention SQL, queries, databases, schemas, tools, or how you executed them.\n"
        "3) NO prefaces like '쿼리 실행했습니다', '데이터베이스에서 찾았습니다', '결과를 보여드릴게요'.\n"
        "4) Be concise. If it's a list, return a clean list; if it's a single value, return just the value with minimal wording.\n"
        "5) If quantities/units matter (e.g., prices, kcal), include them succinctly.\n"
        "\n"
        "Good:\n"
        "- '된장찌개, 비빔밥, 불고기'\n"
        "- '가격 6,000원'\n"
        "Bad:\n"
        "- '쿼리를 실행했습니다. 결과는 다음과 같습니다: …'\n"
        "- '데이터베이스에서 조회한 결과 …'\n"
        "\n"
        "DB schema (for deciding/constructing sql_answer only):\n"
        f"{schema_hints}"
    )

    user_prompt = (
        f"[lang={lang}] {payload.message}\n\n"
        f"---\nContext:\n{context_text if context_text else '(no context)'}"
    )

    messages = [{"role": "system", "content": system_prompt}]
    # 대화 히스토리 포함 -> 멀티턴 대화
    if payload.history:
        messages.extend({"role": m.role, "content": m.content} for m in payload.history)
    messages.append({"role": "user", "content": user_prompt})

    # 4) tool 실행 컨텍스트 주입(ctx)
    ctx: Dict[str, Any] = {
        # SQL
        "engine": getattr(app.state, "engine", None),
        "openai": client,
        "schema_hints": schema_hints,
        "allow_tables": ALLOW_TABLES,
        "sql_max_limit": SQL_MAX_LIMIT,
        "sql_max_rows": SQL_MAX_ROWS,
        # RAG
        "rag_search": lambda q, k, f: search_topk(q, k),
        "rag_max_chars": MAX_CONTEXT_CHARS,
    }

    # 5) 에이전트 1~3스텝 실행 → 실패 시 fallback + 단발 LLM 호출
    try:
        reply_text, usage, tools_used, tool_results, llm_ms, llm_calls = await run_agent_loop(
            client, messages, ctx, max_steps=3
        )
    except Exception as e:
        # fallback: Responses API 단발 호출
        try:
            _t = time.perf_counter()

            tool_choice = (
                {"type": "allowed_tools", "mode": "auto",
                 "tools": [{"type": "function", "name": n} for n in ALLOWED_TOOLS]}
                if ALLOWED_TOOLS else "auto"
            )

            completion = client.responses.create(
                model=OPENAI_MODEL,
                input=messages,
                max_output_tokens=800,
                tool_choice="none",
                reasoning={"effort": REASONING_EFFORT},
                text={"verbosity": VERBOSITY},
            )
            llm_ms = int((time.perf_counter() - _t) * 1000)
            llm_calls = 1
            reply_text = completion.output_text
            usage = getattr(completion, "usage", None)
        except Exception as e2:
            raise HTTPException(
                status_code=500, detail=f"LLM request failed: {e} / fallback: {e2}"
            )
        tools_used, tool_results = [], []

    # 6) 응답
    total_ms = int((time.perf_counter() - t0) * 1000)
    return ChatResponse(
        reply=reply_text,
        model=OPENAI_MODEL,
        usage=(
            usage.model_dump()
            if hasattr(usage, "model_dump")
            else (dict(usage) if usage else None)
        ),
        meta={
            "user_id": payload.user_id,
            "language": lang,
            "rag_used": bool(context_text),
            "rag_sources": rag_sources,  # 프론트 출처 노출용
            "tools_used": tools_used,  # 사용한 tool
            "tool_results_preview": tool_results,
            # 응답속도 관련 지표
            "total_ms": total_ms,  # 요청 수신 → 응답 반환까지 총 소요시간
            "llm_ms": llm_ms,  # LLM API 호출 누적 시간
            "llm_calls": llm_calls,  # LLM 호출 횟수(에이전트 스텝 포함)
        },
    )


# 서버 실행
if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT", "8000")),
        reload=True,
    )
