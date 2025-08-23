# Bapsim Spring Boot Application

Spring Boot와 MySQL을 Docker로 구성한 웹 애플리케이션입니다.

## 🚀 기능

- **Spring Boot 3.2.0** 기반 REST API
- **MySQL 8.0** 데이터베이스
- **Docker Compose**로 간편한 실행
- **JPA/Hibernate**를 사용한 데이터베이스 연동
- **사용자 관리 API** (CRUD 작업)

## 📋 요구사항

- **Java 17** 이상
- **Docker Desktop** (Windows/Mac) 또는 **Docker Engine** (Linux)
- **Gradle** (Gradle Wrapper 포함)

## 🏗️ 프로젝트 구조

```
Bapsim/
├── src/
│   └── main/
│       ├── java/com/bapsim/
│       │   ├── BapsimApplication.java    # 메인 애플리케이션
│       │   ├── controller/
│       │   │   └── UserController.java   # REST API 컨트롤러
│       │   ├── entity/
│       │   │   └── User.java            # 사용자 엔티티
│       │   └── repository/
│       │       └── UserRepository.java   # 데이터 액세스 레이어
│       └── resources/
│           └── application.yml          # 애플리케이션 설정
├── gradle/wrapper/                      # Gradle Wrapper
├── Dockerfile                           # Spring Boot 앱 Docker 이미지
├── docker-compose.yml                   # 서비스 구성
├── init.sql                             # 데이터베이스 초기화
├── build.gradle                         # Gradle 프로젝트 설정
└── README.md                            # 프로젝트 문서
```

## 🚀 빠른 시작

### 1. Docker Desktop 시작
Docker Desktop을 실행하고 Docker가 정상적으로 작동하는지 확인합니다.

### 2. 애플리케이션 실행

```bash
# 모든 플랫폼
docker-compose up --build -d
```

### 3. 애플리케이션 접속
- **Spring Boot App**: http://localhost:8080
- **MySQL Database**: localhost:3306

## 📚 API 엔드포인트

### 테스트
- `GET /api/users/test` - 애플리케이션 상태 확인

### 사용자 관리
- `GET /api/users` - 모든 사용자 조회
- `GET /api/users/{id}` - 특정 사용자 조회
- `POST /api/users` - 새 사용자 생성
- `PUT /api/users/{id}` - 사용자 정보 수정
- `DELETE /api/users/{id}` - 사용자 삭제

### 사용자 생성 예시
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","email":"hong@example.com"}'
```

## 🛠️ 개발 환경

### 로컬 개발 (Docker 없이)
```bash
# Gradle로 빌드 (Gradle이 설치된 경우)
gradle clean compileJava

# 애플리케이션 실행
gradle bootRun
```

### Docker로 개발
```bash
# 이미지 빌드
docker build -t bapsim-app .

# 컨테이너 실행
docker run -p 8080:8080 bapsim-app
```

## 🔧 설정

### 데이터베이스 설정
`src/main/resources/application.yml`에서 데이터베이스 연결 정보를 수정할 수 있습니다:

```yaml
spring:
  datasource:
    url: jdbc:mysql://mysql:3306/bapsim_db
    username: bapsim_user
    password: bapsim_password
```

### Docker 설정
`docker-compose.yml`에서 포트, 환경변수 등을 수정할 수 있습니다.

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
docker-compose logs -f app
docker-compose logs -f mysql
```

### 데이터베이스 접속
```bash
docker exec -it bapsim-mysql mysql -u bapsim_user -p bapsim_db
```

## 🛑 애플리케이션 중지

```bash
# 모든 플랫폼
docker-compose down
```

## 🧹 정리

### 모든 Docker 리소스 정리
```bash
docker-compose down -v
docker system prune -f
```

## 🔍 문제 해결

### 일반적인 문제들

1. **포트 충돌**
   - 8080번 포트가 이미 사용 중인 경우 `docker-compose.yml`에서 포트를 변경

2. **데이터베이스 연결 실패**
   - MySQL 컨테이너가 완전히 시작될 때까지 기다림
   - `docker-compose logs mysql`로 로그 확인

3. **Gradle Wrapper 권한 문제**
   - Docker 빌드 시 `./gradlew: not found` 오류가 발생하는 경우:
   ```bash
   # Windows CMD
   gradlew.bat build

   # Windows PowerShell
   .\gradlew.bat build
   ```
   - 또는 로컬에서 Gradle을 설치하여 사용

4. **권한 문제**
   - Windows에서 PowerShell 실행 정책 문제가 있는 경우:
   ```powershell
   Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
   ```

### 로그 확인
```bash
# 애플리케이션 로그
docker-compose logs app

# 데이터베이스 로그
docker-compose logs mysql
```

## 📝 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.

## 🤝 기여

버그 리포트나 기능 제안은 이슈를 통해 제출해주세요.

---

**Happy Coding! 🎉**
