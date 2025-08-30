package com.bapsim.dto;

import com.bapsim.entity.Payment.PaymentStatus;
import com.bapsim.entity.Payment.PaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {
    
    private Long paymentId;
    private Long userNo;
    private Long menuId;
    private String menuType;
    private String menuName; // 메뉴 이름
    private Integer amount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private Boolean pinVerified;
    private String transactionId;
    private String ssafyTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message; // 응답 메시지
    private String errorCode; // 오류 코드 (실패시)
    private String errorMessage; // 오류 메시지 (실패시)
    
    // 포인트 사용 관련 필드 추가
    private Boolean usePoints;      // 포인트 사용 여부
    private Integer pointAmount;    // 사용된 포인트 금액
    private Integer originalPrice;  // 원래 메뉴 가격
    private Integer finalAmount;    // 포인트 차감 후 최종 결제 금액
    
    // 성공 응답 생성
    public static PaymentResponseDto success(Long paymentId, Long userNo, Long menuId, 
                                          String menuType, String menuName, Integer amount,
                                          PaymentStatus status, PaymentMethod method,
                                          String transactionId, String ssafyTransactionId) {
        return PaymentResponseDto.builder()
                .paymentId(paymentId)
                .userNo(userNo)
                .menuId(menuId)
                .menuType(menuType)
                .menuName(menuName)
                .amount(amount)
                .paymentStatus(status)
                .paymentMethod(method)
                .pinVerified(true)
                .transactionId(transactionId)
                .ssafyTransactionId(ssafyTransactionId)
                .createdAt(LocalDateTime.now())
                .message("결제가 성공적으로 완료되었습니다")
                .usePoints(false)
                .pointAmount(0)
                .originalPrice(amount)
                .finalAmount(amount)
                .build();
    }
    
    // 포인트 사용 시 성공 응답 생성
    public static PaymentResponseDto successWithPoints(Long paymentId, Long userNo, Long menuId, 
                                                     String menuType, String menuName, Integer originalPrice, Integer finalAmount,
                                                     PaymentStatus status, PaymentMethod method,
                                                     String transactionId, String ssafyTransactionId, Integer pointAmount) {
        return PaymentResponseDto.builder()
                .paymentId(paymentId)
                .userNo(userNo)
                .menuId(menuId)
                .menuType(menuType)
                .menuName(menuName)
                .amount(finalAmount)
                .paymentStatus(status)
                .paymentMethod(method)
                .pinVerified(true)
                .transactionId(transactionId)
                .ssafyTransactionId(ssafyTransactionId)
                .createdAt(LocalDateTime.now())
                .message("포인트 사용 결제가 성공적으로 완료되었습니다")
                .usePoints(true)
                .pointAmount(pointAmount)
                .originalPrice(originalPrice)
                .finalAmount(finalAmount)
                .build();
    }
    
    // 실패 응답 생성
    public static PaymentResponseDto failure(String errorCode, String errorMessage) {
        return PaymentResponseDto.builder()
                .paymentStatus(PaymentStatus.FAILED)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .message("결제에 실패했습니다")
                .build();
    }
    
    // PIN 검증 실패 응답 생성
    public static PaymentResponseDto pinVerificationFailed() {
        return PaymentResponseDto.builder()
                .paymentStatus(PaymentStatus.FAILED)
                .errorCode("PIN_VERIFICATION_FAILED")
                .errorMessage("PIN 검증에 실패했습니다")
                .message("PIN을 다시 확인해주세요")
                .build();
    }
    
    // 잔액 부족 응답 생성
    public static PaymentResponseDto insufficientBalance(Integer requiredAmount, Integer currentBalance) {
        return PaymentResponseDto.builder()
                .paymentStatus(PaymentStatus.FAILED)
                .errorCode("INSUFFICIENT_BALANCE")
                .errorMessage("잔액이 부족합니다")
                .message(String.format("필요 금액: %d원, 현재 잔액: %d원", requiredAmount, currentBalance))
                .build();
    }
}
