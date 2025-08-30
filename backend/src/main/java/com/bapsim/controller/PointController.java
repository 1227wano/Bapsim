package com.bapsim.controller;

import com.bapsim.entity.PointHistory;
import com.bapsim.service.PointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 포인트 관리 컨트롤러
 * 포인트 잔액 조회, 내역 조회 등을 처리
 */
@RestController
@RequestMapping("/api/points")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"}, allowCredentials = "false")
public class PointController {
    
    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    
    @Autowired
    private PointService pointService;
    
    /**
     * 사용자 포인트 잔액 조회
     * GET /api/points/balance/{userNo}
     */
    @GetMapping("/balance/{userNo}")
    public ResponseEntity<Map<String, Object>> getPointBalance(@PathVariable Long userNo) {
        log.info("포인트 잔액 조회 API 호출: userNo={}", userNo);
        
        try {
            Integer balance = pointService.getPointBalance(userNo);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("userNo", userNo);
            result.put("pointBalance", balance);
            result.put("message", "포인트 잔액 조회가 완료되었습니다");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("포인트 잔액 조회 중 오류 발생: userNo={}", userNo, e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "BALANCE_QUERY_ERROR");
            result.put("message", "포인트 잔액 조회 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 사용자 포인트 내역 조회 (전체)
     * GET /api/points/history/{userNo}
     */
    @GetMapping("/history/{userNo}")
    public ResponseEntity<Map<String, Object>> getPointHistory(@PathVariable Long userNo) {
        log.info("포인트 내역 조회 API 호출: userNo={}", userNo);
        
        try {
            List<PointHistory> history = pointService.getPointHistory(userNo);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("userNo", userNo);
            result.put("history", history.stream().map(this::convertToHistoryDto).collect(java.util.stream.Collectors.toList()));
            result.put("count", history.size());
            result.put("message", "포인트 내역 조회가 완료되었습니다");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("포인트 내역 조회 중 오류 발생: userNo={}", userNo, e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "HISTORY_QUERY_ERROR");
            result.put("message", "포인트 내역 조회 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 사용자 포인트 적립 내역 조회
     * GET /api/points/history/{userNo}/earn
     */
    @GetMapping("/history/{userNo}/earn")
    public ResponseEntity<Map<String, Object>> getEarnPointHistory(@PathVariable Long userNo) {
        log.info("포인트 적립 내역 조회 API 호출: userNo={}", userNo);
        
        try {
            List<PointHistory> history = pointService.getEarnPointHistory(userNo);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("userNo", userNo);
            result.put("history", history.stream().map(this::convertToHistoryDto).collect(java.util.stream.Collectors.toList()));
            result.put("count", history.size());
            result.put("message", "포인트 적립 내역 조회가 완료되었습니다");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("포인트 적립 내역 조회 중 오류 발생: userNo={}", userNo, e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "EARN_HISTORY_QUERY_ERROR");
            result.put("message", "포인트 적립 내역 조회 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 사용자 포인트 사용 내역 조회
     * GET /api/points/history/{userNo}/use
     */
    @GetMapping("/history/{userNo}/use")
    public ResponseEntity<Map<String, Object>> getUsePointHistory(@PathVariable Long userNo) {
        log.info("포인트 사용 내역 조회 API 호출: userNo={}", userNo);
        
        try {
            List<PointHistory> history = pointService.getUsePointHistory(userNo);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("userNo", userNo);
            result.put("history", history.stream().map(this::convertToHistoryDto).collect(java.util.stream.Collectors.toList()));
            result.put("count", history.size());
            result.put("message", "포인트 사용 내역 조회가 완료되었습니다");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("포인트 사용 내역 조회 중 오류 발생: userNo={}", userNo, e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "USE_HISTORY_QUERY_ERROR");
            result.put("message", "포인트 사용 내역 조회 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    // Private helper methods
    
    /**
     * PointHistory 엔티티를 DTO로 변환
     */
    private Map<String, Object> convertToHistoryDto(PointHistory history) {
        Map<String, Object> dto = new java.util.HashMap<>();
        dto.put("pointId", history.getPointId());
        dto.put("userNo", history.getUserNo());
        dto.put("paymentId", history.getPaymentId());
        dto.put("pointType", history.getPointType());
        dto.put("points", history.getPoints());
        dto.put("balanceAfter", history.getBalanceAfter());
        dto.put("reason", history.getReason());
        dto.put("description", history.getDescription());
        dto.put("createdAt", history.getCreatedAt());
        dto.put("createdId", history.getCreatedId());
        return dto;
    }
}
