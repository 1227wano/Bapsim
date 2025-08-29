package com.bapsim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSAFY API 응답 래퍼 클래스
 * @param <T> 응답 본문 타입
 */
@Data
@NoArgsConstructor
public class SsafyApiResponse<T> {
    
    @JsonProperty("Header")
    private SsafyApiHeader header;
    
    // 응답 본문은 제네릭으로 처리하여 API별로 다른 구조 지원
    private T body;
}
