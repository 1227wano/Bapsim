# Bapsim - 풀스택 대학 식당 관리 시스템

Spring Boot 백엔드, React Native 프론트엔드, Python AI 서비스로 구성된 대학 식당 관리 시스템입니다.

## 🏗️ 프로젝트 구조

```
Bapsim/
├── backend/                    # Spring Boot 백엔드
│   ├── src/main/java/com/bapsim/
│   │   ├── controller/         # REST API 컨트롤러
│   │   ├── service/           # 비즈니스 로직 서비스
│   │   ├── repository/        # 데이터 액세스 레이어
│   │   ├── entity/            # JPA 엔티티
│   │   ├── dto/               # 데이터 전송 객체
│   │   └── config/            # 설정 클래스
│   ├── src/main/resources/    # 설정 파일 및 데이터
│   ├── build.gradle           # Gradle 설정
│   └── Dockerfile             # 백엔드 Docker 이미지
├── frontend/                   # React Native 프론트엔드
│   ├── app/                   # 메인 앱 화면
│   ├── components/            # 재사용 가능한 컴포넌트
│   ├── screens/               # 화면별 스타일
│   ├── package.json           # Node.js 의존성
│   └── Dockerfile             # 프론트엔드 Docker 이미지
├── AI/                        # Python AI 서비스
│   ├── app/                   # AI 애플리케이션 코드
│   ├── agents/                # AI 에이전트 및 도구
│   ├── rag_data/              # RAG 데이터 및 임베딩
│   ├── requirements.txt       # Python 의존성
│   └── Dockerfile             # AI 서비스 Docker 이미지
├── docker-compose.yml         # 전체 서비스 구성
└── README.md                  # 프로젝트 문서
```

## 🏛️ 시스템 구성 다이어그램

### 전체 시스템 아키텍처

```mermaid
graph TB
    subgraph "Client Layer"
        Mobile[Mobile App<br/>React Native]
        Web[Web Browser<br/>React Native Web]
    end
    
    subgraph "Frontend Layer"
        Frontend[Frontend Service<br/>Port 8081]
    end
    
    subgraph "Backend Layer"
        Backend[Backend Service<br/>Spring Boot<br/>Port 8082]
        AI[AI Service<br/>FastAPI<br/>Port 8000]
    end
    
    subgraph "Data Layer"
        MySQL[(MySQL Database<br/>Port 3306)]
        RAG[RAG Data<br/>FAISS Index]
    end
    
    subgraph "External Services"
        SSAFY[SSAFY API<br/>대학 시스템]
        OpenAI[OpenAI API<br/>GPT 모델]
    end
    
    Mobile --> Frontend
    Web --> Frontend
    Frontend --> Backend
    Frontend --> AI
    Backend --> MySQL
    Backend --> SSAFY
    AI --> RAG
    AI --> OpenAI
    AI --> MySQL
    
    style Mobile fill:#e1f5fe
    style Web fill:#e1f5fe
    style Frontend fill:#f3e5f5
    style Backend fill:#e8f5e8
    style AI fill:#fff3e0
    style MySQL fill:#ffebee
    style RAG fill:#f1f8e9
    style SSAFY fill:#e0f2f1
    style OpenAI fill:#fce4ec
```

### 서비스 간 데이터 흐름

```mermaid
sequenceDiagram
    participant U as User
    participant F as Frontend
    participant B as Backend
    participant AI as AI Service
    participant DB as Database
    participant S as SSAFY API
    
    U->>F: 앱 접속
    F->>B: 메뉴 데이터 요청
    B->>DB: 메뉴 조회
    DB-->>B: 메뉴 데이터
    B-->>F: 메뉴 응답
    F-->>U: 메뉴 표시
    
    U->>F: AI 챗봇 질문
    F->>AI: 챗봇 요청
    AI->>DB: 관련 정보 검색
    DB-->>AI: 검색 결과
    AI->>AI: RAG 처리
    AI-->>F: AI 응답
    F-->>U: 챗봇 응답
    
    U->>F: 결제 요청
    F->>B: 결제 처리
    B->>S: 사용자 인증
    S-->>B: 인증 결과
    B->>DB: 결제 정보 저장
    DB-->>B: 저장 완료
    B-->>F: 결제 완료
    F-->>U: 결제 성공
```

### 데이터베이스 스키마

```mermaid
erDiagram
    MEMBER {
        bigint id PK
        string student_id UK
        string name
        string email
        int point_balance
        datetime created_at
        datetime updated_at
    }
    
    CAFETERIAS {
        bigint cafe_no PK
        string name
        string location
        string description
        string operating_hours
    }
    
    RESTAURANTS {
        bigint res_no PK
        string name
        string location
        string description
        string operating_hours
    }
    
    MENUS {
        bigint menu_no PK
        bigint cafe_no FK
        bigint res_no FK
        string menu_name
        string description
        decimal price
        date menu_date
        string meal_type
    }
    
    FOOD {
        bigint food_no PK
        string food_name
        string category
        string description
        string allergens
    }
    
    MENU_PRICE {
        bigint price_no PK
        bigint menu_no FK
        bigint food_no FK
        decimal price
        date effective_date
    }
    
    PAYMENT {
        bigint payment_no PK
        bigint member_id FK
        bigint menu_no FK
        decimal amount
        string payment_method
        string status
        datetime payment_date
    }
    
    POINT_HISTORY {
        bigint history_no PK
        bigint member_id FK
        int point_change
        string reason
        datetime created_at
    }
    
    MEAL_TICKET {
        bigint ticket_no PK
        bigint member_id FK
        bigint menu_no FK
        string status
        datetime issued_at
        datetime used_at
    }
    
    MEMBER ||--o{ PAYMENT : "makes"
    MEMBER ||--o{ POINT_HISTORY : "has"
    MEMBER ||--o{ MEAL_TICKET : "owns"
    CAFETERIAS ||--o{ MENUS : "offers"
    RESTAURANTS ||--o{ MENUS : "offers"
    MENUS ||--o{ MENU_PRICE : "has"
    FOOD ||--o{ MENU_PRICE : "priced"
    MENUS ||--o{ PAYMENT : "purchased"
    MENUS ||--o{ MEAL_TICKET : "issued"
```

### AI 서비스 아키텍처

```mermaid
graph TB
    subgraph "AI Service Layer"
        FastAPI[FastAPI Server]
        RAG[RAG Engine]
        Chatbot[Chatbot Agent]
        Tools[Tool Registry]
    end
    
    subgraph "Data Sources"
        PDF[PDF Documents]
        JSON[JSON Data]
        TXT[Text Files]
        DB[Database]
    end
    
    subgraph "AI Models"
        Embed[Embedding Model<br/>multilingual-e5-base]
        LLM[LLM<br/>GPT-5-mini]
        FAISS[FAISS Index]
    end
    
    subgraph "External APIs"
        OpenAI[OpenAI API]
    end
    
    FastAPI --> RAG
    FastAPI --> Chatbot
    Chatbot --> Tools
    RAG --> Embed
    RAG --> FAISS
    Chatbot --> LLM
    LLM --> OpenAI
    
    PDF --> RAG
    JSON --> RAG
    TXT --> RAG
    DB --> Tools
    
    style FastAPI fill:#ff9800
    style RAG fill:#4caf50
    style Chatbot fill:#2196f3
    style Tools fill:#9c27b0
    style Embed fill:#ff5722
    style LLM fill:#3f51b5
    style FAISS fill:#795548
```

### 주요 워크플로우

#### 사용자 인증 및 로그인 플로우

```mermaid
flowchart TD
    A[사용자 앱 실행] --> B{로그인 상태 확인}
    B -->|로그인됨| C[메인 화면으로 이동]
    B -->|로그인 안됨| D[로그인 화면 표시]
    D --> E[학번/비밀번호 입력]
    E --> F[SSAFY API로 인증]
    F --> G{인증 성공?}
    G -->|성공| H[사용자 정보 조회/생성]
    G -->|실패| I[에러 메시지 표시]
    I --> D
    H --> J[JWT 토큰 발급]
    J --> C
    C --> K[토큰 만료 체크]
    K -->|만료됨| D
    K -->|유효함| L[서비스 계속 이용]
```

#### 메뉴 조회 및 주문 플로우

```mermaid
flowchart TD
    A[메인 화면] --> B[오늘의 메뉴 조회]
    B --> C{메뉴 데이터 존재?}
    C -->|있음| D[메뉴 목록 표시]
    C -->|없음| E[기본 메뉴 표시]
    D --> F[사용자 메뉴 선택]
    E --> F
    F --> G[메뉴 상세 정보]
    G --> H{포인트 잔액 확인}
    H -->|충분함| I[주문 진행]
    H -->|부족함| J[포인트 충전 안내]
    I --> K[주문 수량 선택]
    K --> L[최종 주문 확인]
    L --> M{사용자 확인}
    M -->|확인| N[결제 처리]
    M -->|취소| A
    N --> O{결제 성공?}
    O -->|성공| P[식권 발급]
    O -->|실패| Q[에러 메시지]
    P --> R[주문 완료 화면]
    Q --> A
```

#### AI 챗봇 서비스 플로우

```mermaid
flowchart TD
    A[사용자 질문 입력] --> B[질문 전처리]
    B --> C{질문 유형 분류}
    C -->|메뉴 관련| D[RAG 시스템 검색]
    C -->|일반 문의| E[직접 LLM 처리]
    C -->|시스템 정보| F[도구 실행]
    
    D --> G[FAISS 인덱스 검색]
    G --> H[관련 문서 추출]
    H --> I[컨텍스트 구성]
    I --> J[LLM에 질문 + 컨텍스트 전달]
    
    E --> J
    
    F --> K[적절한 도구 선택]
    K --> L[도구 실행]
    L --> M[결과 수집]
    M --> J
    
    J --> N[AI 응답 생성]
    N --> O[응답 후처리]
    O --> P[사용자에게 응답 표시]
    P --> Q{추가 질문?}
    Q -->|있음| A
    Q -->|없음| R[대화 종료]
```

#### 결제 및 포인트 관리 플로우

```mermaid
flowchart TD
    A[결제 요청] --> B{결제 방식 선택}
    B -->|포인트 결제| C[포인트 잔액 확인]
    B -->|현금 결제| D[현금 결제 처리]
    
    C --> E{잔액 충분?}
    E -->|충분함| F[포인트 차감]
    E -->|부족함| G[포인트 충전 안내]
    
    F --> H[결제 내역 저장]
    D --> H
    
    H --> I[포인트 히스토리 업데이트]
    I --> J[식권 발급]
    J --> K[결제 완료 알림]
    
    G --> L[포인트 충전 화면]
    L --> M[충전 금액 선택]
    M --> N[충전 방법 선택]
    N --> O[충전 처리]
    O --> P{충전 성공?}
    P -->|성공| A
    P -->|실패| Q[에러 메시지]
```

#### 데이터 동기화 및 백업 플로우

```mermaid
flowchart TD
    A[정기 동기화 시작] --> B{동기화 유형}
    B -->|메뉴 데이터| C[SSAFY API 호출]
    B -->|사용자 데이터| D[사용자 정보 업데이트]
    B -->|결제 데이터| E[결제 내역 동기화]
    
    C --> F[새로운 메뉴 데이터 수신]
    F --> G{데이터 변경 감지}
    G -->|변경됨| H[데이터베이스 업데이트]
    G -->|변경 안됨| I[동기화 완료]
    
    D --> J[사용자 정보 비교]
    J --> K{정보 변경?}
    K -->|변경됨| L[사용자 정보 업데이트]
    K -->|변경 안됨| I
    
    E --> M[결제 상태 확인]
    M --> N{상태 변경?}
    N -->|변경됨| O[결제 상태 업데이트]
    N -->|변경 안됨| I
    
    H --> P[변경 로그 기록]
    L --> P
    O --> P
    P --> Q[알림 발송]
    Q --> I
    I --> R[다음 동기화 스케줄링]
```

## 🚀 주요 기능

### 백엔드 (Spring Boot)
- **사용자 관리**: 회원가입, 로그인, 포인트 시스템
- **메뉴 관리**: 카페테리아/레스토랑 메뉴 조회 및 관리
- **결제 시스템**: 식권 구매, 포인트 결제
- **SSAFY API 연동**: 대학 시스템과의 연동

### 프론트엔드 (React Native)
- **크로스 플랫폼**: iOS, Android, Web 지원
- **메뉴 탐색**: 오늘의 메뉴, 주간 식단
- **결제 인터페이스**: 간편한 식권 구매
- **AI 챗봇**: 메뉴 추천 및 문의 응답

### AI 서비스 (Python)
- **RAG 시스템**: 대학 식당 정보 기반 질의응답
- **챗봇**: 사용자 문의에 대한 지능형 응답
- **메뉴 추천**: 개인 취향 기반 메뉴 추천

## 📋 요구사항

### 시스템 요구사항
- **Java 17** 이상
- **Node.js 18** 이상
- **Python 3.9** 이상
- **Docker Desktop** 또는 **Docker Engine**

### 개발 도구
- **Gradle** (백엔드)
- **npm/yarn** (프론트엔드)
- **pip** (AI 서비스)

## 🚀 빠른 시작

### 1. 전체 서비스 실행
```bash
# 모든 서비스 실행
docker-compose up --build -d

# 로그 확인
docker-compose logs -f
```

### 2. 개별 서비스 실행

#### 백엔드만 실행
```bash
cd backend
./gradlew bootRun
```

#### 프론트엔드만 실행
```bash
cd frontend
npm install
npm start
```

#### AI 서비스만 실행
```bash
cd AI
pip install -r requirements.txt
python app/main.py
```

## 🌐 서비스 접속

- **백엔드 API**: http://localhost:8080
- **프론트엔드**: http://localhost:3000 (개발 모드)
- **AI 서비스**: http://localhost:8000

## 📚 API 엔드포인트

### 사용자 관리
- `POST /api/members/login` - 로그인
- `POST /api/members/register` - 회원가입
- `GET /api/members/{id}` - 회원 정보 조회

### 메뉴 관리
- `GET /api/menus` - 전체 메뉴 조회
- `GET /api/menus/cafeteria/{cafeNo}` - 카페테리아 메뉴
- `GET /api/menus/restaurant/{resNo}` - 레스토랑 메뉴
- `GET /api/menus/date/{date}` - 특정 날짜 메뉴

### 결제 시스템
- `POST /api/payments` - 결제 처리
- `GET /api/payments/history` - 결제 내역
- `GET /api/points/balance` - 포인트 잔액

### AI 서비스
- `POST /api/ai/chat` - 챗봇 대화
- `POST /api/ai/recommend` - 메뉴 추천

## 🧪 테스트

### 백엔드 테스트
```bash
cd backend
./gradlew test
```

### 프론트엔드 테스트
```bash
cd frontend
npm test
```

### AI 서비스 테스트
```bash
cd AI
python -m pytest
```

## 🛠️ 개발 환경 설정

### 로컬 개발 (Docker 없이)

#### 백엔드
```bash
cd backend
./gradlew bootRun
```

#### 프론트엔드
```bash
cd frontend
npm install
npm start
```

#### AI 서비스
```bash
cd AI
pip install -r requirements.txt
python app/main.py
```

### Docker 개발
```bash
# 특정 서비스만 빌드
docker-compose build backend
docker-compose build frontend
docker-compose build ai-service

# 특정 서비스만 실행
docker-compose up backend
docker-compose up frontend
docker-compose up ai-service
```

## 🔧 설정

### 환경 변수
`.env` 파일을 생성하여 환경 변수를 설정할 수 있습니다:

```env
# 데이터베이스
DB_HOST=localhost
DB_PORT=3306
DB_NAME=bapsim_db
DB_USER=bapsim_user
DB_PASSWORD=bapsim_password

# AI 서비스
AI_API_KEY=your_api_key
AI_MODEL=gpt-4
```

### 데이터베이스 설정
`backend/src/main/resources/application.yml`에서 데이터베이스 연결 정보를 수정할 수 있습니다.

## 📊 모니터링

### 컨테이너 상태 확인
```bash
docker-compose ps
```

### 로그 확인
```bash
# 모든 서비스 로그
docker-compose logs -f

# 특정 서비스 로그
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f ai-service
```

### 데이터베이스 접속
```bash
docker exec -it bapsim-mysql mysql -u bapsim_user -p bapsim_db
```

## 🛑 서비스 중지

```bash
# 모든 서비스 중지
docker-compose down

# 특정 서비스만 중지
docker-compose stop backend
docker-compose stop frontend
docker-compose stop ai-service
```

## 🧹 정리

### Docker 리소스 정리
```bash
# 모든 컨테이너, 네트워크, 볼륨 삭제
docker-compose down -v

# 사용하지 않는 Docker 리소스 정리
docker system prune -f
```

## 🔍 문제 해결

### 일반적인 문제들

1. **포트 충돌**
   - `docker-compose.yml`에서 포트 매핑 확인
   - 이미 사용 중인 포트 변경

2. **데이터베이스 연결 실패**
   - MySQL 컨테이너 완전 시작 대기
   - `docker-compose logs mysql`로 로그 확인

3. **프론트엔드 빌드 실패**
   - Node.js 버전 확인 (18 이상)
   - `npm cache clean --force` 실행

4. **AI 서비스 오류**
   - Python 버전 확인 (3.9 이상)
   - 의존성 재설치: `pip install -r requirements.txt --force-reinstall`

### 로그 확인
```bash
# 특정 서비스 로그
docker-compose logs backend
docker-compose logs frontend
docker-compose logs ai-service
```

## 📝 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.

## 🤝 기여

버그 리포트나 기능 제안은 이슈를 통해 제출해주세요.

---

**Happy Coding! 🎉**
