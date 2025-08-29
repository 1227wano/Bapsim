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
public class PaymentHistoryDto {
    
    private Long paymentId;
    private Long userNo;
    private Long menuId;
    private String menuType;
    private String menuName;
    private String cafeteriaName; // 식당 이름
    private Integer amount;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private Boolean pinVerified;
    private String transactionId;
    private String ssafyTransactionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 통계 정보 (필요시)
    private String period; // 조회 기간 (TODAY, WEEK, MONTH, YEAR)
    private Integer totalAmount; // 해당 기간 총 결제 금액
    private Long totalCount; // 해당 기간 총 결제 건수
}
