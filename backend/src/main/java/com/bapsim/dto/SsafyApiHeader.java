package com.bapsim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSAFY API 요청/응답 공통 헤더
 */
@Data
@NoArgsConstructor
public class SsafyApiHeader {
    
    @JsonProperty("apiName")
    private String apiName;
    
    @JsonProperty("transmissionDate")
    private String transmissionDate;
    
    @JsonProperty("transmissionTime")
    private String transmissionTime;
    
    @JsonProperty("institutionCode")
    private String institutionCode;
    
    @JsonProperty("fintechAppNo")
    private String fintechAppNo;
    
    @JsonProperty("apiServiceCode")
    private String apiServiceCode;
    
    @JsonProperty("institutionTransactionUniqueNo")
    private String institutionTransactionUniqueNo;
    
    @JsonProperty("apiKey")
    private String apiKey;
    
    @JsonProperty("userKey")
    private String userKey;
    
    // 응답에서 사용하는 필드들
    @JsonProperty("responseCode")
    private String responseCode;
    
    @JsonProperty("responseMessage")
    private String responseMessage;
}
