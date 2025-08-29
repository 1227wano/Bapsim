package com.bapsim.dto;

import com.bapsim.entity.Payment.PaymentMethod;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    
    @NotNull(message = "사용자 번호는 필수입니다")
    private Long userNo;
    
    @NotNull(message = "메뉴 ID는 필수입니다 (숫자)")
    @Min(value = 1, message = "메뉴 ID는 1 이상의 숫자여야 합니다")
    private Long menuId;
    
    @NotNull(message = "메뉴 타입은 필수입니다")
    @Size(min = 1, max = 10, message = "메뉴 타입은 1-10자 사이여야 합니다")
    private String menuType;
    
    // 결제 금액은 메뉴 타입에 따라 자동으로 설정됨 (선택적)
    private Integer amount;
    
    @NotNull(message = "결제 방법은 필수입니다")
    private PaymentMethod paymentMethod;
    
    @NotNull(message = "PIN은 필수입니다")
    @Size(min = 4, max = 4, message = "PIN은 4자리여야 합니다")
    @Pattern(regexp = "^[0-9]{4}$", message = "PIN은 4자리 숫자여야 합니다")
    private String pin;
    
    // 선택적 필드
    private String description; // 결제 설명
    private String cafeteriaId; // 식당 ID (필요시)
    
    // SSAFY API 연동을 위한 필드
    @NotNull(message = "계좌번호는 필수입니다")
    private String accountNo; // 계좌번호 (SSAFY API 필수)
}
