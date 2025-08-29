package com.bapsim.controller;

import com.bapsim.dto.BalanceInquiryResponse;
import com.bapsim.dto.TransactionHistoryResponse;
import com.bapsim.dto.WithdrawalResponse;
import com.bapsim.service.SsafyApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * SSAFY API 테스트용 컨트롤러
 * POSTMAN에서 API 테스트를 위한 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/ssafy")
@RequiredArgsConstructor
public class SsafyApiController {
    
    private final SsafyApiService ssafyApiService;
    
    /**
     * 계좌 잔액 조회 테스트
     * POST /api/ssafy/balance
     */
    @PostMapping("/balance")
    public ResponseEntity<BalanceInquiryResponse> testBalanceInquiry(
            @RequestParam String accountNo) {
        log.info("잔액 조회 테스트 요청: 계좌번호={}", accountNo);
        
        try {
            BalanceInquiryResponse response = ssafyApiService.inquireBalance(accountNo);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("잔액 조회 테스트 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 출금 처리 테스트
     * POST /api/ssafy/withdrawal
     */
    @PostMapping("/withdrawal")
    public ResponseEntity<WithdrawalResponse> testWithdrawal(
            @RequestParam String accountNo,
            @RequestParam BigDecimal amount,
            @RequestParam String summary) {
        log.info("출금 테스트 요청: 계좌번호={}, 금액={}, 요약={}", accountNo, amount, summary);
        
        try {
            WithdrawalResponse response = ssafyApiService.withdrawMoney(accountNo, amount, summary);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("출금 테스트 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 거래내역 조회 테스트
     * POST /api/ssafy/transaction-history
     */
    @PostMapping("/transaction-history")
    public ResponseEntity<TransactionHistoryResponse> testTransactionHistory(
            @RequestParam String accountNo,
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "A") String transactionType,
            @RequestParam(defaultValue = "DESC") String orderByType) {
        log.info("거래내역 조회 테스트 요청: 계좌번호={}, 기간={}~{}, 유형={}", 
                accountNo, startDate, endDate, transactionType);
        
        try {
            TransactionHistoryResponse response = ssafyApiService.inquireTransactionHistory(
                    accountNo, startDate, endDate, transactionType, orderByType);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("거래내역 조회 테스트 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 헬스체크 엔드포인트
     * GET /api/ssafy/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("SSAFY API Controller is running!");
    }
}
