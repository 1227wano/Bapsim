package com.bapsim.service;

import com.bapsim.config.SsafyApiConfig;
import com.bapsim.dto.*;
import com.bapsim.util.SsafyApiHeaderGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * SSAFY API 연동 서비스
 * 실제 API 호출을 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SsafyApiService {
    
    private final SsafyApiConfig ssafyApiConfig;
    private final SsafyApiHeaderGenerator headerGenerator;
    
    @Qualifier("ssafyApiRestTemplate")
    private final RestTemplate restTemplate;
    
    /**
     * 계좌 잔액 조회
     * @param accountNo 계좌번호
     * @return 잔액 조회 결과
     */
    public BalanceInquiryResponse inquireBalance(String accountNo) {
        log.info("잔액 조회 API 호출 시작: 계좌번호={}", accountNo);
        
        try {
            // 헤더 생성 - 실제 API 이름 사용
            SsafyApiHeader header = headerGenerator.generateHeader("inquireDemandDepositAccountBalance");
            
            // API 요청 객체 생성 - SSAFY API 구조에 맞춤
            SsafyApiRequest<BalanceInquiryRequest> request = new SsafyApiRequest<>();
            request.setHeader(header);
            
            // BalanceInquiryRequest body 객체 생성 및 설정
            BalanceInquiryRequest requestBody = new BalanceInquiryRequest();
            requestBody.setAccountNo(accountNo);
            
            // body 객체를 request에 설정
            request.setBody(requestBody);
            
            // API 호출 - 실제 엔드포인트 사용
            String url = ssafyApiConfig.getBaseUrl() + "/demandDeposit/inquireDemandDepositAccountBalance";
            ResponseEntity<BalanceInquiryResponse> response = restTemplate.postForEntity(url, request, BalanceInquiryResponse.class);
            
            log.info("잔액 조회 API 호출 성공: {}", response.getStatusCode());
            return response.getBody();
            
        } catch (Exception e) {
            log.error("잔액 조회 API 호출 실패", e);
            throw new RuntimeException("잔액 조회 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 출금 처리 (결제)
     * @param accountNo 계좌번호
     * @param amount 출금 금액
     * @param summary 출금 요약
     * @return 출금 결과
     */
    public WithdrawalResponse withdrawMoney(String accountNo, BigDecimal amount, String summary) {
        log.info("출금 API 호출 시작: 계좌번호={}, 금액={}, 요약={}", accountNo, amount, summary);
        
        try {
            // 헤더 생성 - 실제 API 이름 사용
            SsafyApiHeader header = headerGenerator.generateHeader("updateDemandDepositAccountWithdrawal");
            
            // API 요청 객체 생성 - SSAFY API 구조에 맞춤
            SsafyApiRequest<WithdrawalRequest> request = new SsafyApiRequest<>();
            request.setHeader(header);
            
            // WithdrawalRequest body 객체 생성 및 설정
            WithdrawalRequest requestBody = new WithdrawalRequest();
            requestBody.setAccountNo(accountNo);
            requestBody.setTransactionBalance(amount.toString());
            requestBody.setTransactionSummary(summary);
            
            // body 객체를 request에 설정
            request.setBody(requestBody);
            
            // API 호출 - 실제 엔드포인트 사용
            String url = ssafyApiConfig.getBaseUrl() + "/demandDeposit/updateDemandDepositAccountWithdrawal";
            ResponseEntity<WithdrawalResponse> response = restTemplate.postForEntity(url, request, WithdrawalResponse.class);
            
            log.info("출금 API 호출 성공: {}", response.getStatusCode());
            return response.getBody();
            
        } catch (Exception e) {
            log.error("출금 API 호출 실패", e);
            throw new RuntimeException("출금 처리 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 거래내역 조회
     * @param accountNo 계좌번호
     * @param startDate 시작일
     * @param endDate 종료일
     * @param transactionType 거래유형
     * @param orderByType 정렬순서
     * @return 거래내역
     */
    public TransactionHistoryResponse inquireTransactionHistory(String accountNo, String startDate, 
                                                             String endDate, String transactionType, String orderByType) {
        log.info("거래내역 조회 API 호출 시작: 계좌번호={}, 기간={}~{}, 유형={}", 
                accountNo, startDate, endDate, transactionType);
        
        try {
            // 헤더 생성 - 실제 API 이름 사용
            SsafyApiHeader header = headerGenerator.generateHeader("inquireTransactionHistoryList");
            
            // API 요청 객체 생성 - SSAFY API 스펙에 맞춤
            TransactionHistoryRequest request = new TransactionHistoryRequest();
            
            // 헤더 정보 설정
            request.setHeader(header);
            
            // 거래내역 조회 파라미터 설정
            request.setAccountNo(accountNo);
            request.setStartDate(startDate);
            request.setEndDate(endDate);
            request.setTransactionType(transactionType);
            request.setOrderByType(orderByType);
            
            log.debug("거래내역 조회 요청 파라미터: {}", request);
            
            // API 호출 - 실제 엔드포인트 사용
            String url = ssafyApiConfig.getBaseUrl() + "/demandDeposit/inquireTransactionHistoryList";
            ResponseEntity<TransactionHistoryResponse> response = restTemplate.postForEntity(url, request, TransactionHistoryResponse.class);
            
            log.info("거래내역 조회 API 호출 성공: {}", response.getStatusCode());
            return response.getBody();
            
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            log.error("거래내역 조회 API 호출 실패 - HTTP 에러: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("거래내역 조회 중 SSAFY API 에러가 발생했습니다: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("거래내역 조회 API 호출 실패 - 일반 에러", e);
            throw new RuntimeException("거래내역 조회 중 오류가 발생했습니다.", e);
        }
    }
}
