-- Bapsim 데이터베이스 초기화 스크립트

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS bapsim_local CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 사용자 생성 (기존 사용자가 있으면 삭제 후 재생성)
DROP USER IF EXISTS 'bapsim_user'@'%';
DROP USER IF EXISTS 'bapsim_user'@'localhost';

-- 새 사용자 생성
CREATE USER 'bapsim_user'@'%' IDENTIFIED BY 'bapsim1234';
CREATE USER 'bapsim_user'@'localhost' IDENTIFIED BY 'bapsim1234';

-- 권한 부여
GRANT ALL PRIVILEGES ON bapsim_local.* TO 'bapsim_user'@'%';
GRANT ALL PRIVILEGES ON bapsim_local.* TO 'bapsim_user'@'localhost';

-- 권한 적용
FLUSH PRIVILEGES;

-- Payment 테이블에 포인트 사용 관련 컬럼 추가
ALTER TABLE Payment ADD COLUMN USE_POINTS BOOLEAN DEFAULT FALSE;
ALTER TABLE Payment ADD COLUMN POINT_AMOUNT INTEGER DEFAULT 0;
ALTER TABLE Payment ADD COLUMN ORIGINAL_PRICE INTEGER;

-- 데이터베이스 상태 확인
SELECT 'Database initialized successfully' as status;
