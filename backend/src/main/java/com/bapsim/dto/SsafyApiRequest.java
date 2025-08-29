package com.bapsim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSAFY API 요청 래퍼 클래스
 * 제네릭 타입을 직접 사용하여 API별로 다른 구조 지원
 * @param <T> 요청 본문 타입
 */
@Data
@NoArgsConstructor
public class SsafyApiRequest<T> {
    
    @JsonProperty("Header")
    private SsafyApiHeader header;
    
    /**
     * 계좌번호 (잔액 조회, 출금, 거래내역 API용)
     * SSAFY API 구조에 맞춰 최상위 레벨에 배치
     */
    @JsonProperty("accountNo")
    private String accountNo;
    
    /**
     * 거래 금액 (출금 API용)
     */
    @JsonProperty("transactionBalance")
    private String transactionBalance;
    
    /**
     * 거래 요약 (출금 API용)
     */
    @JsonProperty("transactionSummary")
    private String transactionSummary;
    
    /**
     * 시작일 (거래내역 조회 API용)
     */
    @JsonProperty("startDate")
    private String startDate;
    
    /**
     * 종료일 (거래내역 조회 API용)
     */
    @JsonProperty("endDate")
    private String endDate;
    
    /**
     * 거래유형 (거래내역 조회 API용)
     */
    @JsonProperty("transactionType")
    private String transactionType;
    
    /**
     * 정렬순서 (거래내역 조회 API용)
     */
    @JsonProperty("orderByType")
    private String orderByType;
    
    // 제네릭 타입의 필드들을 직접 사용
    // SSAFY API 구조에 맞춰 body 객체가 아닌 최상위 레벨에 필드 배치
}
