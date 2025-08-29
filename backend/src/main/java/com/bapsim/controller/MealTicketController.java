package com.bapsim.controller;

import com.bapsim.entity.MealTicket;
import com.bapsim.service.MealTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 식권 관리 컨트롤러 (단순화)
 * 결제 완료 → 식권 발급 → 사용/미사용 상태만 관리
 */
@RestController
@RequestMapping("/api/meal-ticket")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081"}, allowCredentials = "false")
public class MealTicketController {
    
    private static final Logger log = LoggerFactory.getLogger(MealTicketController.class);
    
    @Autowired
    private MealTicketService mealTicketService;
    
    /**
     * 식권 ID로 식권 조회
     * GET /api/meal-ticket/{ticketId}
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<Map<String, Object>> getTicketById(@PathVariable Long ticketId) {
        log.info("식권 조회 API 호출: ticketId={}", ticketId);
        
        try {
            Optional<MealTicket> ticketOpt = mealTicketService.findById(ticketId);
            
            if (ticketOpt.isPresent()) {
                MealTicket ticket = ticketOpt.get();
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", true);
                result.put("ticket", convertToTicketDto(ticket));
                return ResponseEntity.ok(result);
            } else {
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", false);
                result.put("error", "TICKET_NOT_FOUND");
                result.put("message", "식권을 찾을 수 없습니다");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("식권 조회 중 오류 발생: ticketId={}", ticketId, e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "INTERNAL_ERROR");
            result.put("message", "식권 조회 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 사용자별 식권 목록 조회
     * GET /api/meal-ticket/user/{userNo}
     */
    @GetMapping("/user/{userNo}")
    public ResponseEntity<Map<String, Object>> getTicketsByUser(@PathVariable Long userNo) {
        log.info("사용자별 식권 목록 조회 API 호출: userNo={}", userNo);
        
        try {
            List<MealTicket> tickets = mealTicketService.findByUserNo(userNo);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("tickets", tickets.stream().map(this::convertToTicketDto).collect(java.util.stream.Collectors.toList()));
            result.put("count", tickets.size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("사용자별 식권 목록 조회 중 오류 발생: userNo={}", userNo, e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "INTERNAL_ERROR");
            result.put("message", "식권 목록 조회 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 사용자별 사용 가능한 식권 목록 조회 (미사용)
     * GET /api/meal-ticket/user/{userNo}/available
     */
    @GetMapping("/user/{userNo}/available")
    public ResponseEntity<Map<String, Object>> getAvailableTicketsByUser(@PathVariable Long userNo) {
        log.info("사용 가능한 식권 목록 조회 API 호출: userNo={}", userNo);
        
        try {
            List<MealTicket> tickets = mealTicketService.findAvailableTicketsByUser(userNo);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("tickets", tickets.stream().map(this::convertToTicketDto).collect(java.util.stream.Collectors.toList()));
            result.put("count", tickets.size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("사용 가능한 식권 목록 조회 중 오류 발생: userNo={}", userNo, e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "INTERNAL_ERROR");
            result.put("message", "사용 가능한 식권 목록 조회 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 사용자별 사용된 식권 목록 조회 (사용됨)
     * GET /api/meal-ticket/user/{userNo}/used
     */
    @GetMapping("/user/{userNo}/used")
    public ResponseEntity<Map<String, Object>> getUsedTicketsByUser(@PathVariable Long userNo) {
        log.info("사용된 식권 목록 조회 API 호출: userNo={}", userNo);
        
        try {
            List<MealTicket> tickets = mealTicketService.findUsedTicketsByUser(userNo);
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("tickets", tickets.stream().map(this::convertToTicketDto).collect(java.util.stream.Collectors.toList()));
            result.put("count", tickets.size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("사용된 식권 목록 조회 중 오류 발생: userNo={}", userNo, e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "INTERNAL_ERROR");
            result.put("message", "사용된 식권 목록 조회 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 결제 ID로 식권 조회
     * GET /api/meal-ticket/payment/{paymentId}
     */
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<Map<String, Object>> getTicketByPaymentId(@PathVariable Long paymentId) {
        log.info("결제 ID로 식권 조회 API 호출: paymentId={}", paymentId);
        
        try {
            Optional<MealTicket> ticketOpt = mealTicketService.findByPaymentId(paymentId);
            
            if (ticketOpt.isPresent()) {
                MealTicket ticket = ticketOpt.get();
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", true);
                result.put("ticket", convertToTicketDto(ticket));
                return ResponseEntity.ok(result);
            } else {
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", false);
                result.put("error", "TICKET_NOT_FOUND");
                result.put("message", "해당 결제로 발행된 식권을 찾을 수 없습니다");
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("결제 ID로 식권 조회 중 오류 발생: paymentId={}", paymentId, e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "INTERNAL_ERROR");
            result.put("message", "식권 조회 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 식권 사용 처리
     * POST /api/meal-ticket/{ticketId}/use
     */
    @PostMapping("/{ticketId}/use")
    public ResponseEntity<Map<String, Object>> useTicket(@PathVariable Long ticketId, @RequestBody TicketUsageRequest request) {
        log.info("식권 사용 처리 API 호출: ticketId={}, location={}", ticketId, request.getLocation());
        
        try {
            MealTicket usedTicket = mealTicketService.useTicket(ticketId, request.getLocation());
            
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("message", "식권이 성공적으로 사용되었습니다");
            result.put("ticket", convertToTicketDto(usedTicket));
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("식권 사용 처리 중 오류 발생: ticketId={}", ticketId, e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "USAGE_ERROR");
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    // 통계 API 제거 (단순화)
    
    // Private helper methods
    
    /**
     * MealTicket 엔티티를 DTO로 변환
     */
    private Map<String, Object> convertToTicketDto(MealTicket ticket) {
        Map<String, Object> dto = new java.util.HashMap<>();
        dto.put("ticketId", ticket.getTicketId());
        dto.put("paymentId", ticket.getPaymentId());
        dto.put("userNo", ticket.getUserNo());
        dto.put("menuType", ticket.getMenuType());
        dto.put("menuName", ticket.getMenuName());
        dto.put("amount", ticket.getAmount());
        dto.put("issuedAt", ticket.getIssuedAt());
        dto.put("isUsed", ticket.getIsUsed());
        dto.put("usedAt", ticket.getUsedAt());
        dto.put("usedLocation", ticket.getUsedLocation());
        dto.put("createdAt", ticket.getCreatedAt());
        dto.put("updatedAt", ticket.getUpdatedAt());
        dto.put("isAvailable", ticket.isAvailable());
        return dto;
    }
    
    // Request DTOs
    
    /**
     * 식권 사용 요청 DTO
     */
    public static class TicketUsageRequest {
        private String location;
        
        // Getters and Setters
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
    }
}
