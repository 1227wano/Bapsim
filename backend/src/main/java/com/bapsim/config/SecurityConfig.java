package com.bapsim.config; // 본인의 패지 경로에 맞게 수정하세요.

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // stateless한 rest api를 개발할 것이므로 csrf는 disable합니다.
                .csrf(csrf -> csrf.disable())

                // HTTP 요청에 대한 접근 권한을 설정합니다.
                .authorizeHttpRequests(authz -> authz
                        // 아래 경로들은 인증 없이 누구나 접근할 수 있도록 허용합니다.
                        .mvcMatchers(
                                "/",
                                "/api/members/**",       // 로그인, 회원가입 등 회원 관련 API
                                "/swagger-ui/**",       // Swagger UI 페이지
                                "/v3/api-docs/**",      // OpenAPI 3.0 문서
                                "/swagger-resources/**", // Swagger 리소스
                                "/api/ssafy/**"         // SSAFY API 엔드포인트 허용
                        ).permitAll()
                        // 위에서 지정한 경로 외의 모든 요청은 반드시 인증을 받아야 합니다.
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}