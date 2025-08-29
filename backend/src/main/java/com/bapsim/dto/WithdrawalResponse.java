package com.bapsim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 출금 API 응답 DTO
 * SSAFY API 구조에 맞춰 REC 객체 사용
 */
@Data
@NoArgsConstructor
public class WithdrawalResponse {
    
    /**
     * 응답 헤더
     */
    @JsonProperty("Header")
    private SsafyApiHeader header;
    
    /**
     * 거래 정보 (REC 객체)
     */
    @JsonProperty("REC")
    private TransactionInfo transactionInfo;
    
    /**
     * 거래 정보 내부 클래스
     */
    @Data
    @NoArgsConstructor
    public static class TransactionInfo {
        /**
         * 거래 고유 번호
         */
        @JsonProperty("transactionUniqueNo")
        private String transactionUniqueNo;
        
        /**
         * 거래 날짜
         */
        @JsonProperty("transactionDate")
        private String transactionDate;
    }
}
