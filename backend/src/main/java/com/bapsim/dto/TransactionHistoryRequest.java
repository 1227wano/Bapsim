package com.bapsim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 거래내역 조회 API 요청 DTO
 * SSAFY API 스펙에 맞춰 Header와 요청 파라미터를 분리
 */
@Data
@NoArgsConstructor
public class TransactionHistoryRequest {
    
    /**
     * 공통 헤더 정보
     */
    @JsonProperty("Header")
    private SsafyApiHeader header;
    
    /**
     * 계좌번호 (필수) - 16자리
     */
    @JsonProperty("accountNo")
    private String accountNo;
    
    /**
     * 조회 시작일자 (필수) - YYYYMMDD 형식
     */
    @JsonProperty("startDate")
    private String startDate;
    
    /**
     * 조회 종료일자 (필수) - YYYYMMDD 형식
     */
    @JsonProperty("endDate")
    private String endDate;
    
    /**
     * 거래구분 (필수) - M:입금, D:출금, A:전체
     */
    @JsonProperty("transactionType")
    private String transactionType;
    
    /**
     * 정렬순서 (선택) - ASC: 오름차순, DESC: 내림차순
     */
    @JsonProperty("orderByType")
    private String orderByType;
}
