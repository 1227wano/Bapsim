package com.bapsim.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinVerificationDto {
    
    @NotNull(message = "사용자 번호는 필수입니다")
    private Long userNo;
    
    @NotNull(message = "PIN은 필수입니다")
    @Size(min = 4, max = 4, message = "PIN은 4자리여야 합니다")
    private String pin;
    
    // 선택적 필드 - 특정 결제에 대한 PIN 검증시
    private Long paymentId;
    private String purpose; // PIN 검증 목적 (PAYMENT, BALANCE_CHECK 등)
}
