# app/main.py
import os
from typing import Optional, List, Dict, Any
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
import uvicorn
from openai import OpenAI
# 배포시 주석처리
# from dotenv import load_dotenv


# 환경변수 로드
# 배포할 때는 주석처리하고 환경변수 직접 주입
# load_dotenv()


# .env에서 로드
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")
OPENAI_MODEL = os.getenv("OPENAI_MODEL")
APP_ENV = os.getenv("APP_ENV")


# ----- 요청/응답 schema -----
class ChatMessage(BaseModel):
    role: str = Field(..., description="system|user|assistant")
    content: str

# client의 요청 형식
# payload.xx로 불러올 것
class ChatRequest(BaseModel):
    # 요청시 필수 작성 fields
    user_id: str # 사용자 ID
    message: str # 사용자 입력 메시지

    # AI서버 내에서 선택적으로 사용할 fields
    context: Optional[Dict[str, Any]] = None # RAG 데이터 모음
    history: Optional[List[ChatMessage]] = None # 멀티턴 대화를 위한 대화기록
    language: Optional[str] = None # 사용 언어(언어감지모델 붙일 예정.)

class ChatResponse(BaseModel):
    # client에게 보내줄 필수 response fields
    reply: str # LLM의 답변 내용
    model: str # 답변 생성에 사용한 모델

    # 선택적으로 보내줄 fields
    # meta는 미입력시 {} 전달
    usage: Optional[Dict[str, Any]] = None # 크레딧 사용량 / 성능 지표 등
    meta: Dict[str, Any] = Field(default_factory=dict) # 기타 데이터(자유 field)


# ----- FastAPI App -----
app = FastAPI(title="Campus Chatbot API", version="0.1.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=os.getenv("CORS_ALLOW_ORIGINS", "*").split(","),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# AI서버 healthcheck
@app.get("/healthz")
async def healthz():
    return {"status": "ok", "env": APP_ENV}

# chat 엔드포인트
@app.post("/chat", response_model=ChatResponse)
async def chat(payload: ChatRequest):
    # 요청마다 클라이언트 생성
    client = OpenAI(api_key=OPENAI_API_KEY)

    # 시스템 프롬프트
    system_prompt = (
        "You are a multilingual campus assistant for '헤이영 캠퍼스'. "
        "Answer in the user's requested language."
    )

    # 역할, 톤, 응답 가이드라인 등
    messages = [{"role": "system", "content": system_prompt}]
    # 대화 history가 있을 경우 message에 추가
    if payload.history:
        messages.extend({"role": m.role, "content": m.content} for m in payload.history)

    # 감지한 언어
    messages.append({"role": "user", "content": f"[lang={payload.language}] {payload.message}"})

    # OpenAI API에 보낼 데이터 준비
    try:
        completion = client.chat.completions.create(
            model=OPENAI_MODEL, # 환경변수로 모델 선택
            messages=messages,

            # 답변 일관성 / 자유도
            temperature=0.2, # 0~2
            top_p=1.0, # 0~1

            max_tokens = 1000, # 출력토큰제한
        )

        # 응답 추출
        reply_text = completion.choices[0].message.content

        # 응답 변환
        u = getattr(completion, "usage", None)
        usage: Optional[Dict[str, Any]] = (
            u.model_dump() if hasattr(u, "model_dump") else (dict(u) if u else None)
        )

    # OpenAI API 호출 실패시
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"LLM request failed: {e}")

    # 응답 schema
    return ChatResponse(
        reply=reply_text,
        model=OPENAI_MODEL,
        usage=usage,
        meta={"user_id": payload.user_id, "language": payload.language},
    )

# local / container 환경에서 fastapi서버 실행 가능
if __name__ == "__main__":
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=int(os.getenv("PORT")),
        reload=True, # 코드 변경시 자동 재시작
    )