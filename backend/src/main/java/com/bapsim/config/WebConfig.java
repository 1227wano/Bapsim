package com.bapsim.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    // 개발 환경
                    "http://localhost:8081",     // 프론트엔드 개발 서버
                    "http://127.0.0.1:8081",     // localhost 대체 주소
                    "http://localhost:3000",     // React 기본 개발 서버
                    "http://localhost:19006",    // Expo 개발 서버
                    "http://localhost:8082",     // 백엔드 서버 (POSTMAN 테스트용)
                    "http://127.0.0.1:8082",     // 백엔드 서버 대체 주소

                    //AWS 환경
                    "https://bapsim.site",
                    "https://www.bapsim.site"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // 1시간
    }
}
