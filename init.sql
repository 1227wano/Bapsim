-- Bapsim 데이터베이스 초기화 스크립트

USE bapsim_db;

-- users 테이블이 이미 존재하는지 확인하고 삭제
DROP TABLE IF EXISTS users;

-- users 테이블 생성 (JPA가 자동으로 생성하므로 주석 처리)
-- CREATE TABLE users (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     name VARCHAR(255) NOT NULL,
--     email VARCHAR(255) NOT NULL UNIQUE
-- );

-- 샘플 데이터 삽입 (선택사항)
-- INSERT INTO users (name, email) VALUES 
--     ('홍길동', 'hong@example.com'),
--     ('김철수', 'kim@example.com'),
--     ('이영희', 'lee@example.com');

-- 데이터베이스 권한 확인
SHOW GRANTS FOR 'bapsim_user'@'%';
