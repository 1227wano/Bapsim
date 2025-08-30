package com.bapsim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 출금 API 요청 DTO
 * SSAFY API 구조에 맞춰 최상위 레벨에 필드 배치
 */
@Data
@NoArgsConstructor
public class WithdrawalRequest {
    
    /**
     * 계좌번호 (필수)
     */
    @JsonProperty("accountNo")
    private String accountNo;
    
    /**
     * 거래 금액 (필수)
     */
    @JsonProperty("transactionBalance")
    private String transactionBalance;
    
    /**
     * 거래 요약 (필수)
     */
    @JsonProperty("transactionSummary")
    private String transactionSummary;
}
