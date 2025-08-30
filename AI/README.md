## 버전 정보

- 0.1.0

  - RAG-based LLM 챗봇 서버 구현
    - 임베딩화 관련 모듈 전부 포함
    - 임베딩 기반 context 생성, context 포함하여 LLM 질의
  - 로컬 및 컨테이너 서버 테스트 완료
  
  - 이미지 크기 5.83GB



- 0.2.0

  - agents(with 5 tools) 추가
    - 오프토픽 처리
      - tool들 중 하나를 선택하는 방식이라 원하는 대로 작동하지 않음.
      - tool 고르기 전에 먼저 실행되도록 코드 수정 필요
    - PII 처리
    - 추가 정보 요청
      - 일단 작동은 하는데.. 참조할 데이터가 너무 적어서 다양한 케이스 테스트가 어려움
      - 샘플 데이터 추가 후 다시 테스트할 것
    - RAG 참조
      - 샘플데이터 추가 필요
    - SQL 쿼리 실행
      - 스키마 구조 포함 프롬프팅 필요

  - 에이전트 루프 기반으로 필요한 tool 선택 -> 실행 반복 후 최종 반환
  - 이미지 크기 5.86GB



- 0.3.0

  - 호출하는 API GPT-5 계열로 변경
    - GPT-5계열 사용시 make_embeddings.py를 통한 임베딩 생성 불필요
      - RAG tool 주석처리 및 OpenAI가 제공하는 Vectorstore 활용
      - make_openai_vectorstore.py를 통해 OpenAI 서버에 임베딩을 저장
      - 저장소 id를 환경변수로 주입하여 서버에서 로드
  - 추가질문 생성 tool 삭제, 프롬프트로 기능 이동
    - tool 호출이 너무 자주 일어나는 문제가 있어 프롬프트를 통해 유연하게 대응하도록 함
  - 이미지 크기 2.15GB
    - pytorch cpu 전용 버전으로 설치하여 이미지 경량화
  - 아직 참조한 데이터 원문 위주로 답하는 경향이 있어 프롬프트 수정 필요

  

   

## 로컬 서버 실행 코드
```bash
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

## 컨테이너 실행 코드

```bash
docker run -d -p 8000:8000 --name bmpm_ai -v /home/ubuntu/rag_data:/app/rag_data --env-file /home/ubuntu/.env srogsrogi/bmpm_ai_0.3.0
```



## Healthcheck 코드

```cmd
curl -sS http://3.39.192.187:8000/healthz
```



## 테스트 코드



### 기본 테스트 코드

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{ \"user_id\": \"u1\", \"message\": \"오늘 학식 뭐 나와?\" }"
```



### 영어

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{ \"user_id\": \"u2\", \"language\": \"en-US\", \"message\": \"What is for lunch today?\" }"
```



### 일본어

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{ \"user_id\": \"u3\", \"language\": \"ja\", \"message\": \"今日の学食メニューは？\" }"
```



### 멀티턴 대화 동작 확인

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{ \"user_id\": \"u3\", \"language\": \"ko\", \"history\": [ { \"role\": \"assistant\", \"content\": \"어느 캠퍼스/식당/날짜가 궁금한가요?\" } ], \"message\": \"오늘 제2학식 메뉴 알려줘\" }"
```



### RAG tool 호출

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{ \"user_id\": \"u4\", \"language\": \"ko\", \"message\": \"할인 이벤트 하고 있는 거 있어?\" }"
```



### SQL tool 호출

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{ \"user_id\": \"u5\", \"language\": \"ko\", \"message\": \"김치찌개 알러지 정보 알려줘\" }"
```



## off-topic tool 호출

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{ \"user_id\": \"u6\", \"language\": \"ko\", \"message\": \"오늘 날씨 어때?\" }"
```



### PII tool 호출

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{ \"user_id\": \"u7\", \"language\": \"ko\", \"message\": \"나는 김민수야. 전화번호는 01012123789야.\" }"
```



### Clarify tool 호출

```cmd
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{ \"user_id\": \"u7\", \"language\": \"ko\", \"message\": \"그 식당 어딘지 알아?\" }"
```



### 전체 파라미터 포함 API 호출 예시

```bash
curl -X POST http://localhost:8000/chat -H "Content-Type: application/json" -d "{ \"user_id\": \"test_user_123\", \"message\": \"그거 얼만데?\", \"language\": \"ko\", \"history\": [ { \"role\": \"user\", \"content\": \"오늘 제2학식 한식 메뉴 알려줘\" }, { \"role\": \"assistant\", \"content\": \"오늘 학식 메뉴는 제육덮밥입니다.\" } ] }"
```