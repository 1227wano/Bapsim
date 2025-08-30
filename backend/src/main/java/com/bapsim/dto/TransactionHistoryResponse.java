package com.bapsim.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 거래내역 조회 API 응답 DTO
 * SSAFY API 스펙에 맞춰 Header와 REC 객체 구조
 */
@Data
@NoArgsConstructor
public class TransactionHistoryResponse {
    
    /**
     * 응답 헤더 정보
     */
    @JsonProperty("Header")
    private SsafyApiHeader header;
    
    /**
     * 거래내역 데이터
     */
    @JsonProperty("REC")
    private TransactionHistoryData rec;
    
    /**
     * 거래내역 데이터 클래스
     */
    @Data
    @NoArgsConstructor
    public static class TransactionHistoryData {
        
        /**
         * 전체 거래 건수
         */
        @JsonProperty("totalCount")
        private String totalCount;
        
        /**
         * 거래내역 목록
         */
        @JsonProperty("list")
        private List<TransactionDetail> list;
    }
    
    /**
     * 거래 상세 정보 클래스
     */
    @Data
    @NoArgsConstructor
    public static class TransactionDetail {
        
        /**
         * 거래 고유 번호
         */
        @JsonProperty("transactionUniqueNo")
        private String transactionUniqueNo;
        
        /**
         * 거래일자 (YYYYMMDD)
         */
        @JsonProperty("transactionDate")
        private String transactionDate;
        
        /**
         * 거래시간 (HHmmss)
         */
        @JsonProperty("transactionTime")
        private String transactionTime;
        
        /**
         * 거래유형 코드 (1: 입금, 2: 출금)
         */
        @JsonProperty("transactionType")
        private String transactionType;
        
        /**
         * 거래유형명 (입금, 출금, 출금(이체))
         */
        @JsonProperty("transactionTypeName")
        private String transactionTypeName;
        
        /**
         * 거래 계좌번호
         */
        @JsonProperty("transactionAccountNo")
        private String transactionAccountNo;
        
        /**
         * 거래금액
         */
        @JsonProperty("transactionBalance")
        private String transactionBalance;
        
        /**
         * 거래 후 잔액
         */
        @JsonProperty("transactionAfterBalance")
        private String transactionAfterBalance;
        
        /**
         * 거래요약
         */
        @JsonProperty("transactionSummary")
        private String transactionSummary;
        
        /**
         * 거래메모
         */
        @JsonProperty("transactionMemo")
        private String transactionMemo;
    }
}
