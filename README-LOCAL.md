# 🏠 Local Development Environment

## 🚀 Quick Start

### 1. 환경 시작
```bash
docker compose -f docker-compose-local.yml up --build
```

### 2. 환경 중지
```bash
docker compose -f docker-compose-local.yml down
```

### 3. 환경 완전 정리 (데이터 초기화)
```bash
docker compose -f docker-compose-local.yml down -v
```

## 🌐 Service URLs

| Service | URL | Port | Description |
|---------|-----|------|-------------|
| Backend | http://localhost:8082 | 8082 | Spring Boot API |
| Database | localhost:3306 | 3306 | MySQL Database |

## 🔧 Database Connection

- **Host**: localhost
- **Port**: 3306
- **Database**: bapsim_local
- **Username**: bapsim_user
- **Password**: bapsim1234

## 📝 Troubleshooting

### 데이터베이스 연결 문제
```bash
# MySQL 컨테이너에 직접 접속
docker exec -it bapsim-db-local mysql -u root -proot1234

# 사용자 확인
SELECT User, Host FROM mysql.user WHERE User = 'bapsim_user';

# 데이터베이스 확인
SHOW DATABASES;
```

### 로그 확인
```bash
# 전체 로그
docker compose -f docker-compose-local.yml logs

# 실시간 로그
docker compose -f docker-compose-local.yml logs -f

# 특정 서비스 로그
docker compose -f docker-compose-local.yml logs backend
```
