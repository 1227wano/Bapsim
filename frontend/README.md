# Bapsim Frontend

## 환경 설정

### API URL 설정

프로젝트의 API URL을 환경별로 설정할 수 있습니다.

#### 1. 환경변수 파일 생성

프로젝트 루트에 `.env` 파일을 생성하고 다음 내용을 추가하세요:

```bash
# 개발 환경
EXPO_PUBLIC_API_URL=http://localhost:8082

# 프로덕션 환경 (AWS 등)
#//API_BASE_URL: process.env.EXPO_PUBLIC_API_URL || 'http://3.34.126.173',

#### 2. 환경별 설정

- **개발 환경**: `http://localhost:8082`
- **프로덕션 환경**: `hhttp://3.34.126.173`

#### 3. 설정 적용

환경변수를 변경한 후에는 앱을 재시작해야 합니다:

```bash
# Expo 개발 서버 재시작
npm start
# 또는
yarn start
```

### Config 파일 구조

`constants/Config.ts` 파일에서 모든 API 엔드포인트를 관리합니다:

```typescript
export const Config = {
  API_BASE_URL: process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8082',
  API_ENDPOINTS: {
    SIGNATURE_MENUS: '/api/menus/signature',
    DATE_MENUS: '/api/menus/date',
    // ... 기타 엔드포인트
  },
  getApiUrl: (endpoint: string) => `${Config.API_BASE_URL}${endpoint}`,
};
```

### 사용 예시

```typescript
import { Config } from '../constants/Config';

// API URL 생성
const url = Config.getApiUrl(Config.API_ENDPOINTS.SIGNATURE_MENUS);
// 결과: "http://localhost:8082/api/menus/signature" (개발환경)
// 결과: "https://your-aws-domain.com/api/menus/signature" (프로덕션)
```

## 설치 및 실행

```bash
npm install
npm start
```

## 주요 기능

- 오늘의 메뉴 조회
- 메뉴 상세 정보
- 결제 시스템
- PIN 번호 관리
- 로그인/회원가입
