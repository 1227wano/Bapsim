// 환경별 설정
export const Config = {
  // API 기본 URL
  API_BASE_URL: process.env.EXPO_PUBLIC_API_URL || 'http://192.168.11.239:8082',
  // API_BASE_URL: process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8082',
//   API_BASE_URL: process.env.EXPO_PUBLIC_API_URL || 'http://3.34.126.173',
  
  // API 엔드포인트들
  API_ENDPOINTS: {
    // 메뉴 관련
    SIGNATURE_MENUS: '/api/menus/signature',
    DATE_MENUS: '/api/menus/date',
    RESTAURANT_MENUS: '/api/menus/restaurant',
    MENU_FOOD: '/api/menus',
    MENU_PRICES: '/api/menus/prices/meal-type',
    
    // 대학교 관련
    UNIVERSITIES: '/api/universities',
    
    // 회원 관련
    MEMBER_LOGIN: '/api/members/login',
    MEMBER_INFO: '/api/members',
    
    // 결제 관련
    PAYMENT_VALIDATE: '/api/payment/validate',
    PAYMENT_REGISTER_PIN: '/api/payment/register-pin',
  },
  
  // 전체 API URL 생성 함수
  getApiUrl: (endpoint: string) => `${Config.API_BASE_URL}${endpoint}`,
  
  // 환경 정보
  ENV: process.env.NODE_ENV || 'development',
  IS_PRODUCTION: process.env.NODE_ENV === 'production',
  IS_DEVELOPMENT: process.env.NODE_ENV === 'development',
};

// 사용 예시:
// Config.getApiUrl(Config.API_ENDPOINTS.SIGNATURE_MENUS)
// 결과: "http://localhost:8082/api/menus/signature" (개발환경)
// 결과: "https://your-aws-domain.com/api/menus/signature" (프로덕션)

// AWS 배포 시 환경변수 설정:
// EXPO_PUBLIC_API_URL=https://your-aws-domain.com
