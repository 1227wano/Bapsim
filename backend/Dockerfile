FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Gradle 파일들 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .

# 소스 코드 복사
COPY src src

# 직접 Gradle을 사용하여 빌드
RUN gradle clean build -x test --no-daemon --refresh-dependencies

# 실행 단계
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드된 JAR 파일 복사 (Spring Boot 실행 가능 JAR)
# COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
