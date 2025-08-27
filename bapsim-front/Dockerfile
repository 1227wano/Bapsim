# 1. Build Stage
FROM node:20-alpine AS build
WORKDIR /app

# package.json과 lock 파일을 먼저 복사하여 종속성을 캐싱합니다.
COPY package.json package-lock.json* ./
RUN npm install

# 소스 코드를 복사합니다.
COPY . .

# Expo 프로젝트를 웹 정적 파일로 빌드합니다. (수정된 부분)
# npx를 사용하여 프로젝트에 명시된 버전의 expo를 실행합니다.
RUN npx expo export -p web

# 2. Run Stage
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

# Nginx 설정 파일을 복사합니다.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 빌드된 정적 파일들을 Nginx의 기본 경로로 복사합니다.
COPY --from=build /app/dist .

# 80 포트를 노출합니다.
EXPOSE 80

# Nginx 실행
CMD ["nginx", "-g", "daemon off;"]
