package com.bapsim.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SSAFY API 설정 클래스
 * application.yml의 ssafy.api 설정을 바인딩
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ssafy.api")
public class SsafyApiConfig {
    
    /**
     * SSAFY API 기본 URL
     */
    private String baseUrl;
    
    /**
     * API 헤더 설정
     */
    private Headers headers = new Headers();
    
    /**
     * API 엔드포인트 설정
     */
    private Endpoints endpoints = new Endpoints();
    
    /**
     * 타임아웃 설정
     */
    private Timeout timeout = new Timeout();
    
    @Data
    public static class Headers {
        private String institutionCode;
        private String fintechAppNo;
        private String apiKey;
        private String userKey;
    }
    
    @Data
    public static class Endpoints {
        private String balanceInquiry;
        private String withdrawal;
        private String transactionHistory;
    }
    
    @Data
    public static class Timeout {
        private int connect;
        private int read;
    }
}
