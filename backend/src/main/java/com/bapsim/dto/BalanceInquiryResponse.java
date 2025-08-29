package com.bapsim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 잔액 조회 API 응답 DTO
 * SSAFY API 구조에 맞춰 REC 객체 사용
 */
@Data
@NoArgsConstructor
public class BalanceInquiryResponse {
    
    /**
     * 응답 헤더
     */
    @JsonProperty("Header")
    private SsafyApiHeader header;
    
    /**
     * 계좌 정보 (REC 객체)
     */
    @JsonProperty("REC")
    private AccountInfo accountInfo;
    
    /**
     * 계좌 정보 내부 클래스
     */
    @Data
    @NoArgsConstructor
    public static class AccountInfo {
        @JsonProperty("bankCode")
        private String bankCode;
        
        @JsonProperty("accountNo")
        private String accountNo;
        
        @JsonProperty("accountBalance")
        private String accountBalance;
        
        @JsonProperty("accountCreatedDate")
        private String accountCreatedDate;
        
        @JsonProperty("accountExpiryDate")
        private String accountExpiryDate;
        
        @JsonProperty("lastTransactionDate")
        private String lastTransactionDate;
        
        @JsonProperty("currency")
        private String currency;
    }
}
