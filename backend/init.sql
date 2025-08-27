-- Bapsim 데이터베이스 초기화 스크립트

USE bapsim_db;

-- root 사용자에게 모든 권한 부여 (외부 연결 허용)
GRANT ALL PRIVILEGES ON bapsim_db.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

-- 데이터베이스 상태 확인
SELECT 'Database initialized successfully' as status;
