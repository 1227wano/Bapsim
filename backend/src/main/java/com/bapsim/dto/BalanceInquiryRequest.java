package com.bapsim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 잔액 조회 API 요청 DTO
 * SSAFY API 구조에 맞춰 accountNo를 최상위 레벨에 배치
 */
@Data
@NoArgsConstructor
public class BalanceInquiryRequest {
    /**
     * 계좌번호 (필수) - 최상위 레벨에 배치
     * SSAFY API 요구사항에 맞춤
     */
    @JsonProperty("accountNo")
    private String accountNo;
}
