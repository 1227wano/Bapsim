package com.bapsim.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate Bean 설정 클래스
 * SSAFY API 호출을 위한 전용 RestTemplate 설정
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * SSAFY API 전용 RestTemplate Bean
     * 타임아웃 설정과 에러 핸들링을 포함
     */
    @Bean("ssafyApiRestTemplate")
    public RestTemplate ssafyApiRestTemplate() {
        // HTTP 요청 팩토리 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // 연결 타임아웃 설정 (5초)
        factory.setConnectTimeout(5000);
        
        // 읽기 타임아웃 설정 (10초)
        factory.setReadTimeout(10000);
        
        // RestTemplate 생성 및 설정
        RestTemplate restTemplate = new RestTemplate(factory);
        
        return restTemplate;
    }
}
