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

```cmd
curl -sS -X POST http://3.39.192.187:8000/chat -H "Content-Type: application/json" -d "{\"user_id\":\"u1\",\"message\":\"진행되고 있는 이벤트 알려줘\",\"language\":\"ko\"}"
```

