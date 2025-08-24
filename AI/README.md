## 버전 정보

- 0.1.0

  - 임베딩화 관련 모듈 전부 포함
  - 임베딩 기반 context 생성, context 포함하여 LLM 질의
  - .env
    - OPENAI_API_KEY=
    - OPENAI_MODEL=
    - EMBED_MODEL_NAME=
    - RAG_TOP_K=
    - RAG_MAX_CONTEXT_CHARS=
    - RAG_INDEX_DIR=
    
  - 로컬 테스트 완료
  
  - 이미지 크기 5.83GB



## 컨테이너 실행 코드

```bash
docker run -d -p 8000:8000 --name bmpm_ai -v /home/ubuntu/rag_data:/app/rag_data --env-file /home/ubuntu/.env srogsrogi/bmpm_ai_0.1.0
```



## Healthcheck 코드

```cmd
curl -sS http://3.39.192.187:8000/healthz
```



## 테스트 코드



### 기본 테스트 코드

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{""user_id"":""u1"",""message"":""오늘 학식 뭐 나와?""}"
```



### 영어

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{""user_id"":""u2"",""language"":""en-US"",""message"":""What is for lunch today?""}"
```



### 일본어

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{""user_id"":""u3"",""language"":""ja"",""message"":""今日の学食メニューは？""}"
```



### 멀티턴 대화 동작 확인

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{""user_id"":""u3"",""language"":""ko"",""history"":[{""role"":""assistant"",""content"":""어느 캠퍼스/식당/날짜가 궁금한가요?""}],""message"":""오늘 제2학식 메뉴 알려줘""}"
```



### RAG tool 호출

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{""user_id"":""u4"",""language"":""ko"",""message"":""할인 이벤트 하고 있는 거 있어?""}"
```



### SQL tool 호출

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{""user_id"":""u5"",""language"":""ko"",""message"":""김치찌개 알러지 정보 알려줘""}"
```



## off-topic tool 호출

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{""user_id"":""u6"",""language"":""ko"",""message"":""오늘 날씨 어때?""}"
```



### PII tool 호출

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{""user_id"":""u7"",""language"":""ko"",""message"":""나는 김민수야. 전화번호는 01012123789야.""}"
```



### Clarify tool 호출

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{""user_id"":""u7"",""language"":""ko"",""message"":""그 식당 어딘지 알아?""}"
```

