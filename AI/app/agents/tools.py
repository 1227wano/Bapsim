# app/agents/tools.py
from typing import Any, Dict, List, Optional, Tuple, Callable
from pydantic import BaseModel
import re, unicodedata
from rapidfuzz import fuzz, process
from unidecode import unidecode

# ===== 공통 유틸리티 =====
# 문장 전처리 함수
def _normalize(t: str) -> str:
    # 유니코드 정규화 후, 연속 공백을 단일 공백으로 축약하고 양끝 공백은 제거
    return re.sub(r"\s+", " ", unicodedata.normalize("NFKC", t or "")).strip()

def _normalize_loose(t: str) -> str:
    # 악센트/다이어크리틱 제거 + 소문자 + 공백 축약 (오타/다국어 변형에 강함)
    t = unicodedata.normalize("NFKC", t or "")
    t = unidecode(t).lower()
    return re.sub(r"\s+", " ", t).strip()

# 도메인 시소러스(핵심만 가볍게) — 필요시 계속 보강
DOMAIN_SYNONYMS = {
    "menu": [
        # ko
        "학식","메뉴","식단","오늘 뭐 나와","영양","알레르기","알러지","원산지","학생식당","교내식당","푸드코트",
        # en
        "menu","cafeteria","canteen","dining hall","nutrition","allergy","ingredients","lunch today",
        # zh
        "食堂","菜单","食谱","今日菜单","营养","过敏原",
        # es
        "menu","comedor","cafeteria","carta","nutricion","alergia","almuerzo de hoy",
    ],
    "payment": [
        "결제","포인트","쿠폰","적립","스탬프","리워드","신한페이",
        "payment","pay","points","coupon","reward","stamp",
        "支付","积分","优惠券","奖励",
        "pago","puntos","cupon","recompensa",
    ],
    "campus": [
        "캠퍼스","교내","학교","학내","후생관",
        "campus","student center","on campus",
        "校内","校园",
        "campus universitario",
    ],
}

# 평면화된 레퍼런스(중복 제거)
DOMAIN_LEXICON = sorted(set(sum(DOMAIN_SYNONYMS.values(), [])))

# --- 민감정보 정의 ---
_PII_RX = [
    (re.compile(r'([0-9]{3})-?([0-9]{3,4})-?([0-9]{4})'), r'\1-****-\3'),
    (re.compile(r'([A-Za-z0-9._%+-]+)@([A-Za-z0-9.-]+\.[A-Za-z]{2,})'), r'***@\2'),
    (re.compile(r'(?:학번|student\s*id)[:\s]*([0-9]{8,10})', re.I), r'학번: ********'),
    (re.compile(r'(?:계좌|account)[:\s]*([0-9\-]{8,})', re.I), r'계좌: ****(masked)'),
]

# 입력 텍스트에서 PII를 찾아 치환하고 검출여부(hit)와 검출한 종류(kinds)를 반환하는 함수
def redact_pii(text: str):
    red, hit, kinds = text, False, []
    for rx, repl in _PII_RX:
        if rx.search(red):
            hit = True; kinds.append(rx.pattern[:24]+"...")
            red = rx.sub(repl, red)
    return red, hit, kinds

# --- 오프토픽 판별 ---
def _is_domain_by_fuzzy(text: str, min_ratio: int = 80, min_hits: int = 1) -> bool:
    """
    간단·빠른 1차 게이트:
      - 전체 문장 vs 키워드: 부분 유사도(오타/띄어쓰기 흔들림 흡수)
      - 토큰별 근접 유사도: 단어단위 보조 매칭
    """
    q = _normalize_loose(text)
    if not q:
        return False

    # 1) 전체 문장과 각 키워드의 부분 유사도
    hit = 0
    for kw in DOMAIN_LEXICON:
        if fuzz.partial_ratio(q, _normalize_loose(kw)) >= min_ratio:
            hit += 1
            if hit >= min_hits:
                return True

    # 2) 토큰 단위 보조 매칭 (짧은 토큰 제외)
    tokens = [w for w in re.split(r"[^a-z0-9\uac00-\ud7af]+", q) if len(w) > 2]
    for w in tokens:
        best = process.extractOne(w, DOMAIN_LEXICON, scorer=fuzz.ratio)
        if best and best[1] >= min_ratio:
            hit += 1
            if hit >= min_hits:
                return True

    return False

def is_offtopic(text: str) -> bool:
    # 퍼지 매칭으로 온/오프 판정 (True면 오프토픽)
    return not _is_domain_by_fuzzy(text, min_ratio=80, min_hits=1)

# --- Clarify 슬롯 체크 ---
def need_clarify(text: str, known=None):
    lo = _normalize(text).lower(); ks = known or {}; need=[]
    if not (ks.get("date") or any(k in lo for k in ["오늘","내일","202","월","일"])): need.append("날짜")
    if not (ks.get("place") or any(k in lo for k in ["학생식당","교내식당","후생관","푸드코트","학식"])): need.append("식당/매장")
    if not (ks.get("campus") or any(k in lo for k in ["캠퍼스","학교","서강대","연세대","동국대","서울대"])): need.append("캠퍼스/학교")
    return None if not need else f"정확히 안내하려면 {'·'.join(need)}가 필요해요. 어떤 {'·'.join(need)}인가요?"

# --- SQL Guard ---
# SELECT 외 쿼리는 거부
# 사용하는 SQL 계정도 Read-only 권한이지만 이중 보안 적용
DENY = ("update","delete","insert","drop","alter","grant","revoke","create","truncate")
def guard_sql(sql: str, allow_tables=None, force_limit=200):
    s = (sql or "").strip().rstrip(";"); low = s.lower()
    if not low.startswith("select "): return False,"","ONLY_SELECT"
    if any(d in low for d in DENY): return False,"","BAD_KEYWORD"
    if allow_tables:
        tables = re.findall(r'\bfrom\s+([a-zA-Z_][\w]*)', low)+re.findall(r'\bjoin\s+([a-zA-Z_][\w]*)', low)
        if not all(t in allow_tables for t in tables): return False,"","DENY_TABLE"
    if " limit " not in low: s+=f" LIMIT {force_limit}"
    return True, s+";", ""

# ====== Tool #1: 오프토픽 ======
class OfftopicInput(BaseModel):
    question: str
def offtopic_router_run(args, ctx):
    q=args["question"]
    if is_offtopic(q):
        return {"offtopic":True,"message":"이 기능은 학식/교내 식당 정보 검색 도우미예요.","examples":["오늘 학식 영양정보","후생관 인기메뉴", "What’s today’s cafeteria menu?"]}
    return {"offtopic":False}

# ====== Tool #2: SQL ======
class SQLAnswerInput(BaseModel):
    question: str
    schema_ddl: str
    known_filters: Optional[Dict[str,Any]]=None
    proposed_sql: Optional[str]=None

def sql_answer_run(args, ctx):
    from sqlalchemy import text
    eng = ctx.get("engine")
    client = ctx.get("openai")   # main.py에서 넣어줘야 함
    if not eng:
        return {"error": "DB_NOT_READY"}

    # 1) SQL 확보: 있으면 그대로 쓰고, 없으면 LLM 호출
    sql = args.get("proposed_sql") or ""
    if not sql:
        schema_ddl = args.get("schema_ddl") or ctx.get("schema_hints", "")
        q = args.get("question", "")
        prompt = f"""
        You are a SQL generator. Based on the schema:

        {schema_ddl}

        Generate ONE safe SELECT SQL query (no explanation, no comments).
        User question: {q}
        """
        mdl = ctx.get("sql_llm_model", "gpt-4o-mini")
        try:
            resp = client.chat.completions.create(
                model=mdl,
                messages=[
                    {"role":"system","content":"You generate a single SELECT SQL only."},
                    {"role":"user","content": prompt}
                ],
                temperature=0.0,
                max_tokens=300,
            )
            sql = resp.choices[0].message.content.strip()
        except Exception as e:
            return {"error": "LLM_SQL_ERROR", "detail": str(e)}

    # 2) 안전성 검사
    ok, safe, reason = guard_sql(sql, ctx.get("allow_tables"), ctx.get("sql_max_limit",200))
    if not ok:
        return {"error":"SQL_BLOCKED","reason":reason,"sql":sql}

    # 3) 실행
    rows, cols = [], []
    try:
        with eng.connect() as c:
            res = c.execute(text(safe))
            cols = list(res.keys())
            for i, r in enumerate(res):
                if i >= ctx.get("sql_max_rows",200): break
                rows.append(list(r))
    except Exception as e:
        return {"error":"SQL_EXEC_ERROR","detail":str(e)[:200], "sql":safe}

    # 4) 결과 반환 → 다음 루프에서 LLM이 자연어 답변 생성
    return {"safe_sql": safe, "cols": cols, "rows": rows}

# ====== Tool #3: RAG ======
class RAGLookupInput(BaseModel):
    query: str; filters: Optional[Dict[str,Any]]=None; top_k:int=6
def rag_lookup_run(args, ctx):
    f=ctx.get("rag_search");
    if not f: return {"error":"RAG_NOT_READY"}
    hits=f(_normalize(args["query"]),args.get("top_k",6),args.get("filters") or {})
    return {"context":"\n".join(h["text"] for h in hits),"sources":[h.get("meta",{}) for h in hits]}

# ====== Tool #4: Clarify ======
class ClarifyInput(BaseModel):
    question: str; known_slots: Optional[Dict[str,Any]]=None
def clarify_builder_run(args, ctx):
    q=args["question"]; known=args.get("known_slots") or {}
    s=need_clarify(q,known); return {"need_clarify":bool(s),"short_question":s or ""}

# ====== Tool #5: PII ======
class SafetyRedactorInput(BaseModel):
    text: str; policy: Optional[str]="mask"
def safety_redactor_run(args, ctx):
    red,hit,kinds=redact_pii(args["text"])
    if args.get("policy")=="reject" and hit: return {"rejected":True,"found":kinds}
    return {"rejected":False,"text_redacted":red,"pii_detected":hit,"found":kinds}

# ===== Registry =====
TOOLS_SPEC=[
  {"type":"function","function":{"name":"offtopic_router","description":"비관련 질문 필터","parameters":OfftopicInput.model_json_schema()}},
  {"type":"function","function":{"name":"sql_answer","description":"DB 조회 및 SQL 실행","parameters":SQLAnswerInput.model_json_schema()}},
  {"type":"function","function":{"name":"rag_lookup","description":"임베딩 검색","parameters":RAGLookupInput.model_json_schema()}},
  {"type":"function","function":{"name":"clarify_builder","description":"모호 질문 재질문","parameters":ClarifyInput.model_json_schema()}},
  {"type":"function","function":{"name":"safety_redactor","description":"PII 마스킹/차단","parameters":SafetyRedactorInput.model_json_schema()}}
]
TOOLS_EXEC={
  "offtopic_router":offtopic_router_run,
  "sql_answer":sql_answer_run,
  "rag_lookup":rag_lookup_run,
  "clarify_builder":clarify_builder_run,
  "safety_redactor":safety_redactor_run,
}
def run_tool_safely(name,args,ctx):
    fn=TOOLS_EXEC.get(name)
    try: return fn(args,ctx)
    except Exception as e: return {"error":str(e)}