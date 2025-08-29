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
from app.agents.tools import TOOLS_SPEC, TOOLS_EXEC, run_tool_safely

# -------------------------
# 환경변수/설정
# -------------------------
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-5-mini")  # 기본값 gpt-5-mini
APP_ENV = os.getenv("APP_ENV")  # 개발/배포 환경 구분

# GPT-5 튜닝 옵션
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

    # OPENAI Vector store 준비
    # OpenAI 클라이언트 생성/저장
    try:
        app.state.openai = OpenAI(api_key=OPENAI_API_KEY)
        print("[OPENAI] client ready")
    except Exception as e:
        app.state.openai = None
        print(f"[OPENAI] init failed: {e}")
    
    # vector store 확인
    try:
        vs_ids = [s.strip() for s in os.getenv("VECTOR_STORE_ID", "").split(",") if s.strip()]
        if vs_ids:
            for vid in vs_ids:
                meta = app.state.openai.vector_stores.retrieve(vid)
                print(f"[FS] vector_store {vid} status={getattr(meta, 'status', None)}")
        else:
            print("[FS] VECTOR_STORE_ID not set")
    except Exception as e:
        print(f"[FS] vector_store retrieve failed: {e}")

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
    Responses API 기반 에이전트 루프.
    빌트인 툴(file_search)은 OpenAI가 내부적으로 처리
    함수 툴(sql_answer 등)만 run_tool_safely로 실행 후 tool 메시지를 이어붙여 재호출
    """
    messages = list(base_messages)
    tools_used: List[str] = []
    tool_results: List[Dict[str, Any]] = []
    llm_ms = 0
    llm_calls = 0


    # 함수 툴 이름 집합(TOOLS_SPEC에서 type == "function"인 항목들)
    function_tool_names = {t.get("name") for t in TOOLS_SPEC if t.get("type") == "function"}

    for _ in range(max_steps):
        t0 = time.perf_counter()
        # API 호출 전에 tool 역할 메시지 필터링
        filtered_messages = [m for m in messages if m.get("role") != "tool"]
        resp = client.responses.create(
            model=OPENAI_MODEL,
            input=filtered_messages,
            tools=TOOLS_SPEC,           # functions + built-in tools(file_search 등)
            tool_choice="auto",    # required/allowed_tools/auto 등 외부에서 결정
            max_output_tokens=800,
            reasoning={"effort": REASONING_EFFORT},
            text={"verbosity": VERBOSITY},
        )

        # 디버깅: 응답 전체 덤프
        for it in (getattr(resp, "output", []) or []):
            print("  - item.type:", getattr(it, "type", None),
                  "name:", getattr(it, "name", None),
                  "role:", getattr(it, "role", None))

        # 총 응답시간
        llm_ms += int((time.perf_counter() - t0) * 1000)
        # 총 호출 수
        llm_calls += 1

        out_text = getattr(resp, "output_text", None)
        output_items = getattr(resp, "output", []) or []
        tool_calls = [it for it in output_items if (getattr(it, "type", None) or "").endswith("_call")]

        # 툴콜이 없으면(모델이 바로 답함) 즉시 반환
        if not tool_calls:
            if out_text:
                return (
                    out_text,
                    getattr(resp, "usage", None),
                    tools_used,
                    tool_results,
                    llm_ms,
                    llm_calls,
                )
            # 텍스트도 없으면 루프 종료 → 아래 finalize로
            break

        # 빌트인/함수 툴 분리
        func_calls = [tc for tc in tool_calls if getattr(tc, "name", None) in function_tool_names]
        builtin_calls = [tc for tc in tool_calls if getattr(tc, "name", None) not in function_tool_names]

        # 빌트인 툴 호출은 기록만 (OpenAI가 내부적으로 실행)
        for tc in builtin_calls:
            # 지금은 built-in tool이 RAG밖에 없음. 기능 추가시 로깅도 수정 필요
            tools_used.append(getattr(tc, "name", None) or "file_search")

        # 실행할 함수 툴이 하나도 없고, 이미 모델의 텍스트가 있다면 그걸로 종료
        if not func_calls:
            if out_text:
                return (
                    out_text,
                    getattr(resp, "usage", None),
                    tools_used,
                    tool_results,
                    llm_ms,
                    llm_calls,
                )
            else:
                # 텍스트가 없으면 한 번 더 호출한 후 마무리
                t1 = time.perf_counter()
                final = client.responses.create(
                    model=OPENAI_MODEL,
                    input=messages,
                    max_output_tokens=800,
                    tool_choice="none",
                    reasoning={"effort": REASONING_EFFORT},
                    text={"verbosity": VERBOSITY},
                )
                llm_ms += int((time.perf_counter() - t1) * 1000)
                llm_calls += 1
                return (
                    final.output_text,
                    getattr(final, "usage", None),
                    tools_used,
                    tool_results,
                    llm_ms,
                    llm_calls,
                )

        # 함수 툴 실행 → tool 메시지 붙이기
        for tc in func_calls:
            name = getattr(tc, "name", None)
            raw_args = getattr(tc, "arguments", {}) or {}
            try:
                args = raw_args if isinstance(raw_args, dict) else json.loads(raw_args)
            except Exception:
                args = {}

            result = run_tool_safely(name, args, ctx)

            # 도구가 직접적인 답변을 생성하는 tool들을 사용하는 경우, 즉시 루프를 종료하고 결과를 반환
            if name == "offtopic_router" and result.get("offtopic"):
                return result.get("message"), None, [name], [{"name": name, "result": result}], llm_ms, llm_calls
            if name == "clarify_builder" and result.get("need_clarify"):
                return result.get("short_question"), None, [name], [{"name": name, "result": result}], llm_ms, llm_calls

            tools_used.append(name)
            tool_results.append({"name": name, "keys": list(result.keys())[:8]})

            messages.append({
                "role": "tool",
                "tool_call_id": getattr(tc, "id", None),
                "content": json.dumps(result, ensure_ascii=False),
            })

        # 다음 스텝에서 툴 결과를 추가하여 재호출

    # 스텝 초과 또는 조기 종료 케이스 → 최종 생성 한 번
    t2 = time.perf_counter()
    # 최종 응답 생성 시에는 tool 역할 메시지 제외 (API 규격)
    final_messages = [m for m in messages if m.get("role") != "tool"]
    final = client.responses.create(
        model=OPENAI_MODEL,
        input=final_messages,
        max_output_tokens=800,
        tool_choice="none",
        reasoning={"effort": REASONING_EFFORT},
        text={"verbosity": VERBOSITY},
    )
    llm_ms += int((time.perf_counter() - t2) * 1000)
    llm_calls += 1

    # 서버 로그
    print("[DEBUG] final response]", (final.model_dump_json(indent=2) if hasattr(final, "model_dump_json") else str(final)))

    # 최종 반환
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
      3) 에이전트 루프 실행(최대 2스텝), 실패 시 단발 LLM 호출로 fallback
      4) 응답 + 메타데이터(지연/툴/출처/사용량 등) 반환
    """
    t0 = time.perf_counter()  # 총 지연시간
    client = getattr(app.state, "openai", None) or OpenAI(api_key=OPENAI_API_KEY)

    # 언어는 클라이언트가 언어감지모델 실행해서 보낸 값을 신뢰
    # 주요 언어가 아닌 것들은 따로 프롬프트 추가하지 않고 LLM 자체 기능에 맡김
    # 식문화 관련해서 context에 추가할 내용이 있는 언어는 프롬프트 추가하는 코드 구현 예정
    lang = (payload.language or "ko").strip()


    # context에 추가할 DB schema 구조
    schema_hints = '''
CREATE TABLE `account` (
  `user_no` bigint NOT NULL,
  `account_no` bigint NOT NULL,
  `password` varchar(255) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`user_no`),
  CONSTRAINT `FK_account_member` FOREIGN KEY (`user_no`) REFERENCES `member` (`user_no`)
);

CREATE TABLE `ai_service` (
  `session_id` int NOT NULL AUTO_INCREMENT,
  `user_no` bigint NOT NULL,
  `user_message` varchar(500) NOT NULL,
  `assistant_message` varchar(500) DEFAULT NULL,
  `usage_prompt_token` int DEFAULT NULL,
  `usage_completion_token` int DEFAULT NULL,
  `usage_total_tokens` int DEFAULT NULL,
  `llm_latency_ms` int DEFAULT NULL,
  `total_latency_ms` int DEFAULT NULL,
  `rag_used` bit(1) DEFAULT NULL,
  `language` varchar(5) DEFAULT NULL,
  PRIMARY KEY (`session_id`),
  KEY `FK_aiservice_member` (`user_no`),
  CONSTRAINT `FK_aiservice_member` FOREIGN KEY (`user_no`) REFERENCES `member` (`user_no`)
);

CREATE TABLE `cafeterias` (
  `cafe_no` bigint NOT NULL AUTO_INCREMENT,
  `uni_id` int NOT NULL,
  `build_name` varchar(100) NOT NULL,
  `phone_no` bigint NOT NULL,
  `open_time` datetime(6) NOT NULL,
  `close_time` datetime(6) NOT NULL,
  `run_yn` varchar(1) NOT NULL,
  `del_yn` varchar(1) NOT NULL,
  `visitor` bigint NOT NULL,
  `created_id` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_id` varchar(100) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`cafe_no`),
  KEY `FK_cafeterias_university` (`uni_id`),
  CONSTRAINT `FK_cafeterias_university` FOREIGN KEY (`uni_id`) REFERENCES `university` (`uni_id`)
);

CREATE TABLE `food` (
  `food_no` bigint NOT NULL AUTO_INCREMENT,
  `menu_name` varchar(100) NOT NULL,
  `kcal` bigint NOT NULL,
  `allergy` bigint NOT NULL,
  `category` varchar(10) NOT NULL,
  `content` varchar(500) DEFAULT NULL,
  `photo_path` varchar(500) DEFAULT NULL,
  `allergy_info` varchar(500) DEFAULT NULL,
  `menu_no` bigint DEFAULT NULL,
  PRIMARY KEY (`food_no`),
  KEY `FK_food_menus` (`menu_no`),
  CONSTRAINT `FK_food_menus` FOREIGN KEY (`menu_no`) REFERENCES `menus` (`menu_no`)
);

CREATE TABLE `member` (
  `user_no` bigint NOT NULL AUTO_INCREMENT,
  `uni_id` int NOT NULL,
  `user_id` varchar(100) NOT NULL,
  `user_pass` varchar(500) NOT NULL,
  `user_name` varchar(100) NOT NULL,
  `user_email` varchar(100) DEFAULT NULL,
  `user_phone` varchar(20) DEFAULT NULL,
  `user_type` varchar(20) NOT NULL,
  `user_status` varchar(20) NOT NULL,
  `created_id` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_id` varchar(100) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`user_no`),
  KEY `FK_member_university` (`uni_id`),
  CONSTRAINT `FK_member_university` FOREIGN KEY (`uni_id`) REFERENCES `university` (`uni_id`)
);

CREATE TABLE `menu_price` (
  `price_no` bigint NOT NULL AUTO_INCREMENT,
  `kind` varchar(1) NOT NULL,
  `meal_type` varchar(100) NOT NULL,
  `price` bigint NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `effective_date` date NOT NULL,
  `expiry_date` date DEFAULT NULL,
  `is_active` bit(1) NOT NULL,
  `created_id` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_id` varchar(100) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `menu_no` bigint DEFAULT NULL,
  PRIMARY KEY (`price_no`),
  KEY `FK_menuprice_menus` (`menu_no`),
  CONSTRAINT `FK_menuprice_menus` FOREIGN KEY (`menu_no`) REFERENCES `menus` (`menu_no`)
);

CREATE TABLE `menus` (
  `menu_no` bigint NOT NULL AUTO_INCREMENT,
  `kind` varchar(1) NOT NULL,
  `meal_type` varchar(100) NOT NULL,
  `is_signature` bit(1) NOT NULL,
  `created_id` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_id` varchar(100) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `sold_out` bit(1) NOT NULL,
  `cafe_no` bigint DEFAULT NULL,
  `res_no` bigint DEFAULT NULL,
  `menu_date` date DEFAULT NULL,
  PRIMARY KEY (`menu_no`),
  KEY `FK_menus_cafeterias` (`cafe_no`),
  KEY `FK_menus_restaurants` (`res_no`),
  CONSTRAINT `FK_menus_cafeterias` FOREIGN KEY (`cafe_no`) REFERENCES `cafeterias` (`cafe_no`),
  CONSTRAINT `FK_menus_restaurants` FOREIGN KEY (`res_no`) REFERENCES `restaurants` (`res_no`)
);

CREATE TABLE `payment` (
  `payment_id` bigint NOT NULL AUTO_INCREMENT,
  `user_no` bigint NOT NULL,
  `amount` int NOT NULL,
  `transaction_id` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `FK_payment_member` (`user_no`),
  CONSTRAINT `FK_payment_member` FOREIGN KEY (`user_no`) REFERENCES `member` (`user_no`)
);

CREATE TABLE `point_history` (
  `point_id` bigint NOT NULL AUTO_INCREMENT,
  `user_no` bigint NOT NULL,
  `payment_id` bigint DEFAULT NULL,
  `point_changed` int DEFAULT NULL,
  `reason` varchar(50) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`point_id`),
  KEY `FK_pointhistory_member` (`user_no`),
  KEY `FK_pointhistory_payment` (`payment_id`),
  CONSTRAINT `FK_pointhistory_member` FOREIGN KEY (`user_no`) REFERENCES `member` (`user_no`),
  CONSTRAINT `FK_pointhistory_payment` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`payment_id`)
);

CREATE TABLE `restaurants` (
  `res_no` bigint NOT NULL AUTO_INCREMENT,
  `res_name` varchar(100) NOT NULL,
  `address` varchar(2000) NOT NULL,
  `phone_no` bigint NOT NULL,
  `open_time` datetime(6) NOT NULL,
  `close_time` datetime(6) NOT NULL,
  `run_yn` varchar(1) NOT NULL,
  `del_yn` varchar(1) NOT NULL,
  `visitor` bigint NOT NULL,
  `created_id` varchar(100) NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_id` varchar(100) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`res_no`)
);

CREATE TABLE `university` (
  `uni_id` int NOT NULL AUTO_INCREMENT,
  `uni_name` varchar(30) NOT NULL,
  PRIMARY KEY (`uni_id`)
);
'''

    # 오늘 날짜 로드
    today = time.strftime("%Y-%m-%d")  # 2025-01-01

    # 3) 프롬프트 구성
    system_prompt =f"""
    당신은 SSAFY university의 학식/교내 식당 정보제공 도우미입니다.
    주 업무: 메뉴 및 가격·영양·알레르기·교내 매장 정보·결제/포인트·이벤트 등에 대한 질의 응답을 담당합니다.
    오늘 날짜는 {today}입니다.
    
    [도구 사용 원칙]
    - offtopic_router: 질문이 학식/교내 도메인과 무관하면 먼저 호출
    - safety_redactor: 개인정보(전화·이메일·학번·계좌)가 있으면 policy에 따라 masking 또는 reject 처리
    - sql_answer: 메뉴/가격/영양/알러지 등 DB 기반 질문일 때 SELECT 쿼리로 조회
      * 반드시 SELECT만 허용, DML/DDL 금지
      * LIMIT 누락 시 강제로 추가
      * 허용된 테이블만 사용
    - file_search/RAG: 질문이 운영시간, 이벤트, 공지사항, 규칙, 정책 등에 관련된 경우 호출
    - 질문에 장소, 시간 등 필수 정보가 누락되어 명확한 답변이 어려우면, 다른 도구를 사용하기 전에 먼저 사용자에게 되물어 명확히 하세요.

    [답변 스타일]
    - 사용자의 언어로 답변 (기본 한국어)
    - 간결·친절·정확. 핵심 먼저, 필요 시 세부사항 추가

    [포맷 규칙]
    - 필요 시 목록/표 활용 (과도하게 복잡 X)
    - 알레르기/원산지는 ⚠️ 등 간단한 경고 기호 사용 가능
    - RAG 문맥 사용 시 간단히 출처명 표기

    [안전·정합성]
    - 개인정보는 반드시 마스킹
    - 확신이 없으면 "확인 불가"라고 답하고, 다음 행동 제안
    - 가격/영양 및 알러지 정보 등은 반드시 DB 기반으로 대답

    [SQL 세부 규칙]
    - 한 번에 하나의 SELECT만
    - 스키마 제약 준수, LIMIT 내에서 요약
    - 결과가 필요할 때는 SQL만 출력 (코드블록/주석 없이)

    [DB SCHEMA HINTS]
    {schema_hints}
    """

    # 언어감지결과 같이 오면, system_prompt로 통합할 듯
    user_prompt = (
        f"[lang={lang}] {payload.message}\n\n"
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
        "rag_search": lambda q, k: search_topk(q, k),
        "rag_max_chars": MAX_CONTEXT_CHARS,
    }

    # 5) 에이전트 1~2스텝 실행 → 실패 시 fallback + 단발 LLM 호출
    try:
        reply_text, usage, tools_used, tool_results, llm_ms, llm_calls = await run_agent_loop(
            client, messages, ctx, max_steps=2
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
                tools=TOOLS_SPEC,
                max_output_tokens=800,
                tool_choice="auto",
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