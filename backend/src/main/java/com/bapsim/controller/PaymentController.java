package com.bapsim.controller;

import com.bapsim.dto.*;
import com.bapsim.entity.Payment;
import com.bapsim.service.PaymentService;
import com.bapsim.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
@Validated
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8081", "http://localhost:19006"}, allowCredentials = "false")
public class PaymentController {
    
    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);
    
    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;
    
    /**
     * 결제 전 검증
     * POST /api/payment/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<PaymentValidationDto> validatePayment(@Valid @RequestBody PaymentRequestDto requestDto) {
        try {
            log.info("결제 검증 요청 시작: userNo={}, menuId={}, amount={}, accountNo={}", 
                    requestDto.getUserNo(), requestDto.getMenuId(), requestDto.getAmount(), 
                    requestDto.getAccountNo() != null ? "제공됨" : "누락됨");
            
            PaymentValidationDto validation = paymentService.validatePayment(requestDto);
            log.info("결제 검증 성공: userNo={}, isBalanceSufficient={}, isMenuAvailable={}", 
                    requestDto.getUserNo(), validation.getIsBalanceSufficient(), validation.getIsMenuAvailable());
            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            log.error("결제 검증 중 오류 발생: userNo={}, error={}", 
                    requestDto.getUserNo(), e.getMessage(), e);
            
            PaymentValidationDto errorResponse = PaymentValidationDto.builder()
                    .userNo(requestDto.getUserNo())
                    .menuId(requestDto.getMenuId())
                    .menuType(requestDto.getMenuType())
                    .isBalanceSufficient(false)
                    .isMenuAvailable(false)
                    .errorCode("VALIDATION_ERROR")
                    .validationMessage("검증 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * JSON 파싱 오류 처리
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleJsonParseError(org.springframework.http.converter.HttpMessageNotReadableException e) {
        log.error("JSON 파싱 오류 발생: {}", e.getMessage());
        
        String errorMessage = "요청 데이터 형식이 올바르지 않습니다. ";
        if (e.getMessage().contains("menuId")) {
            errorMessage += "menuId는 숫자여야 합니다.";
        } else if (e.getMessage().contains("userNo")) {
            errorMessage += "userNo는 숫자여야 합니다.";
        } else if (e.getMessage().contains("amount")) {
            errorMessage += "amount는 숫자여야 합니다.";
        } else {
            errorMessage += "모든 필수 필드가 올바른 형식으로 제공되어야 합니다.";
        }
        
        Map<String, Object> errorResponse = new java.util.HashMap<>();
        errorResponse.put("error", "JSON_PARSE_ERROR");
        errorResponse.put("message", errorMessage);
        errorResponse.put("timestamp", LocalDateTime.now());
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    /**
     * PIN 검증
     * POST /api/payment/verify-pin
     */
    @PostMapping("/verify-pin")
    public ResponseEntity<Object> verifyPin(@Valid @RequestBody PinVerificationDto pinDto) {
        try {
            boolean isValid = paymentService.verifyPin(pinDto);
            if (isValid) {
                Map<String, Object> successResponse = new java.util.HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "PIN 검증이 성공했습니다");
                successResponse.put("timestamp", LocalDateTime.now());
                return ResponseEntity.ok(successResponse);
            } else {
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "PIN이 올바르지 않습니다");
                errorResponse.put("errorCode", "PIN_VERIFICATION_FAILED");
                errorResponse.put("timestamp", LocalDateTime.now());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "PIN 검증 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("errorCode", "INTERNAL_ERROR");
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * PIN 등록 (최초 등록)
     * POST /api/payment/register-pin
     */
    @PostMapping("/register-pin")
    public ResponseEntity<PinRegistrationResponseDto> registerPin(@RequestBody PinRegistrationDto pinDto) {
        try {
            PinRegistrationResponseDto response = paymentService.registerPin(pinDto);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            PinRegistrationResponseDto errorResponse = PinRegistrationResponseDto.failure(
                "PIN_REGISTRATION_ERROR", 
                "PIN 등록 중 오류가 발생했습니다: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * PIN 수정
     * PUT /api/payment/update-pin
     */
    @PutMapping("/update-pin")
    public ResponseEntity<PinRegistrationResponseDto> updatePin(@Valid @RequestBody PinRegistrationDto pinDto) {
        try {
            PinRegistrationResponseDto response = paymentService.updatePin(pinDto);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            PinRegistrationResponseDto errorResponse = PinRegistrationResponseDto.failure(
                "PIN_UPDATE_ERROR", 
                "PIN 수정 중 오류가 발생했습니다: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * PIN 삭제
     * DELETE /api/payment/delete-pin/{userNo}
     */
    @DeleteMapping("/delete-pin/{userNo}")
    public ResponseEntity<PinRegistrationResponseDto> deletePin(
            @PathVariable Long userNo,
            @RequestParam String currentPin) {
        try {
            PinRegistrationResponseDto response = paymentService.deletePin(userNo, currentPin);
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            PinRegistrationResponseDto errorResponse = PinRegistrationResponseDto.failure(
                "PIN_DELETION_ERROR", 
                "PIN 삭제 중 오류가 발생했습니다: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * PIN 등록 상태 확인
     * GET /api/payment/pin-status/{userNo}
     */
    @GetMapping("/pin-status/{userNo}")
    public ResponseEntity<Object> getPinStatus(@PathVariable Long userNo) {
        try {
            boolean isRegistered = paymentService.isPinRegistered(userNo);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("userNo", userNo);
            response.put("isPinRegistered", isRegistered);
            response.put("message", isRegistered ? "PIN이 등록되어 있습니다" : "PIN이 등록되어 있지 않습니다");
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "PIN 상태 확인 중 오류가 발생했습니다: " + e.getMessage());
            errorResponse.put("errorCode", "INTERNAL_ERROR");
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 결제 처리
     * POST /api/payment/process
     */
    @PostMapping("/process")
    public ResponseEntity<PaymentResponseDto> processPayment(@Valid @RequestBody PaymentRequestDto requestDto) {
        log.info("결제 처리 요청 시작: userNo={}, menuId={}, amount={}", 
                requestDto.getUserNo(), requestDto.getMenuId(), requestDto.getAmount());
        
        try {
            PaymentResponseDto response = paymentService.processPayment(requestDto);
            
            if (response.getPaymentStatus() == Payment.PaymentStatus.COMPLETED) {
                log.info("결제 처리 성공: paymentId={}, userNo={}", 
                        response.getPaymentId(), response.getUserNo());
                return ResponseEntity.ok(response);
            } else {
                log.warn("결제 처리 실패: userNo={}, errorCode={}", 
                        requestDto.getUserNo(), response.getErrorCode());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            log.error("결제 처리 중 예외 발생: userNo={}, error={}", 
                    requestDto.getUserNo(), e.getMessage(), e);
            PaymentResponseDto errorResponse = PaymentResponseDto.failure(
                "PAYMENT_PROCESS_ERROR", 
                "결제 처리 중 오류가 발생했습니다: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 결제 내역 조회
     * GET /api/payment/history/{userNo}
     */
    @GetMapping("/history/{userNo}")
    public ResponseEntity<List<PaymentHistoryDto>> getPaymentHistory(
            @PathVariable Long userNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<PaymentHistoryDto> history = paymentService.getPaymentHistory(userNo, startDate, endDate);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 결제 상세 조회
     * GET /api/payment/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPaymentDetail(@PathVariable Long paymentId) {
        try {
            log.info("결제 상세 조회 API 호출: paymentId={}", paymentId);
            
            Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
            if (paymentOpt.isPresent()) {
                Payment payment = paymentOpt.get();
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", true);
                result.put("payment", convertToDetailDto(payment));
                return ResponseEntity.ok(result);
            } else {
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("success", false);
                result.put("error", "PAYMENT_NOT_FOUND");
                result.put("message", "결제 내역을 찾을 수 없습니다");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("결제 상세 조회 중 오류 발생: paymentId={}", paymentId, e);
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", false);
            result.put("error", "INTERNAL_ERROR");
            result.put("message", "결제 조회 중 오류가 발생했습니다");
            return ResponseEntity.internalServerError().body(result);
        }
    }
    
    /**
     * 결제 취소
     * POST /api/payment/{paymentId}/cancel
     */
    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponseDto> cancelPayment(
            @PathVariable Long paymentId,
            @RequestParam Long userNo) {
        try {
            PaymentResponseDto response = paymentService.cancelPayment(paymentId, userNo);
            
            if (response.getPaymentStatus() == Payment.PaymentStatus.CANCELLED) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception e) {
            PaymentResponseDto errorResponse = PaymentResponseDto.failure(
                "CANCEL_ERROR", 
                "결제 취소 중 오류가 발생했습니다: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    
    /**
     * 메뉴 타입별 결제 내역
     * GET /api/payment/menu-type/{menuType}
     */
    @GetMapping("/menu-type/{menuType}")
    public ResponseEntity<List<PaymentHistoryDto>> getPaymentsByMenuType(
            @PathVariable String menuType,
            @RequestParam(required = false) Long userNo) {
        try {
            // TODO: PaymentService에 getPaymentsByMenuType 메서드 추가 필요
            // List<PaymentHistoryDto> payments;
            // if (userNo != null) {
            //     payments = paymentService.getPaymentsByMenuTypeAndUser(menuType, userNo);
            // } else {
            //     payments = paymentService.getPaymentsByMenuType(menuType);
            // }
            // return ResponseEntity.ok(payments);
            
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 결제 상태별 조회
     * GET /api/payment/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentHistoryDto>> getPaymentsByStatus(
            @PathVariable String status,
            @RequestParam(required = false) Long userNo) {
        try {
            // TODO: PaymentService에 getPaymentsByStatus 메서드 추가 필요
            // List<PaymentHistoryDto> payments;
            // if (userNo != null) {
            //     payments = paymentService.getPaymentsByStatusAndUser(status, userNo);
            // } else {
            //     payments = paymentService.getPaymentsByStatus(status);
            // }
            // return ResponseEntity.ok(payments);
            
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 오늘의 결제 내역
     * GET /api/payment/today/{userNo}
     */
    @GetMapping("/today/{userNo}")
    public ResponseEntity<List<PaymentHistoryDto>> getTodayPayments(@PathVariable Long userNo) {
        try {
            LocalDateTime today = LocalDateTime.now();
            LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = today.toLocalDate().atTime(23, 59, 59);
            
            List<PaymentHistoryDto> todayPayments = paymentService.getPaymentHistory(userNo, startOfDay, endOfDay);
            return ResponseEntity.ok(todayPayments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 결제 가능한 메뉴 타입 조회
     * GET /api/payment/available-menu-types
     */
    @GetMapping("/available-menu-types")
    public ResponseEntity<Object> getAvailableMenuTypes() {
        try {
            // 현재 날짜 기준으로 사용 가능한 메뉴 타입과 가격 정보 조회
            java.time.LocalDate today = java.time.LocalDate.now();
            List<com.bapsim.entity.MenuPrice> availablePrices = 
                paymentService.getAvailableMenuPrices(today);
            
            List<Map<String, Object>> menuTypes = availablePrices.stream()
                .map(mp -> {
                    Map<String, Object> menuMap = new java.util.HashMap<>();
                    menuMap.put("kind", mp.getKind());
                    menuMap.put("mealType", mp.getMealType());
                    menuMap.put("description", mp.getDescription());
                    menuMap.put("price", mp.getPrice());
                    menuMap.put("effectiveDate", mp.getEffectiveDate());
                    return menuMap;
                })
                .collect(java.util.stream.Collectors.toList());
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("availableMenuTypes", menuTypes);
            response.put("currentDate", today);
            response.put("message", "현재 사용 가능한 메뉴 타입과 가격 정보입니다");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("메뉴 타입 조회 중 오류 발생", e);
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "메뉴 타입 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * 특정 메뉴 타입의 가격 조회
     * GET /api/payment/menu-price/{menuType}
     */
    @GetMapping("/menu-price/{menuType}")
    public ResponseEntity<Object> getMenuPrice(@PathVariable String menuType) {
        try {
            Integer price = paymentService.getMenuPriceByType(menuType);
            
            if (price != null) {
                Map<String, Object> response = new java.util.HashMap<>();
                response.put("menuType", menuType);
                response.put("price", price);
                response.put("message", "메뉴 타입 " + menuType + "의 현재 가격입니다");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> errorResponse = new java.util.HashMap<>();
                errorResponse.put("error", "MENU_PRICE_NOT_FOUND");
                errorResponse.put("message", "메뉴 타입 " + menuType + "에 대한 가격 정보를 찾을 수 없습니다");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
        } catch (Exception e) {
            log.error("메뉴 타입 {}의 가격 조회 중 오류 발생", menuType, e);
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("error", "메뉴 가격 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    /**
     * Payment 엔티티를 상세 조회용 DTO로 변환
     */
    private Map<String, Object> convertToDetailDto(Payment payment) {
        Map<String, Object> dto = new java.util.HashMap<>();
        dto.put("paymentId", payment.getPaymentId());
        dto.put("userNo", payment.getUserNo());
        dto.put("menuId", payment.getMenuId());
        dto.put("menuType", payment.getMenuType());
        dto.put("amount", payment.getAmount());
        dto.put("paymentStatus", payment.getPaymentStatus());
        dto.put("paymentMethod", payment.getPaymentMethod());
        dto.put("pinVerified", payment.getPinVerified());
        dto.put("transactionId", payment.getTransactionId());
        dto.put("ssafyTransactionId", payment.getSsafyTransactionId());
        dto.put("createdAt", payment.getCreatedAt());
        dto.put("updatedAt", payment.getUpdatedAt());
        dto.put("createdId", payment.getCreatedId());
        dto.put("updatedId", payment.getUpdatedId());
        return dto;
    }
}
