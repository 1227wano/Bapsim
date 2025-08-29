package com.bapsim.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentValidationDto {
    
    private Long userNo;
    private Long menuId;
    private String menuType;
    private String menuName;
    private Integer menuPrice;
    private Integer userBalance;
    private Boolean isBalanceSufficient;
    private Boolean isMenuAvailable;
    private String validationMessage;
    private String errorCode;
    
    // 검증 성공 응답 생성
    public static PaymentValidationDto success(Long userNo, Long menuId, String menuType, 
                                             String menuName, Integer menuPrice, Integer userBalance) {
        return PaymentValidationDto.builder()
                .userNo(userNo)
                .menuId(menuId)
                .menuType(menuType)
                .menuName(menuName)
                .menuPrice(menuPrice)
                .userBalance(userBalance)
                .isBalanceSufficient(userBalance >= menuPrice)
                .isMenuAvailable(true)
                .validationMessage("결제 검증이 완료되었습니다")
                .build();
    }
    
    // 잔액 부족 응답 생성
    public static PaymentValidationDto insufficientBalance(Long userNo, Long menuId, String menuType,
                                                         String menuName, Integer menuPrice, Integer userBalance) {
        return PaymentValidationDto.builder()
                .userNo(userNo)
                .menuId(menuId)
                .menuType(menuType)
                .menuName(menuName)
                .menuPrice(menuPrice)
                .userBalance(userBalance)
                .isBalanceSufficient(false)
                .isMenuAvailable(true)
                .errorCode("INSUFFICIENT_BALANCE")
                .validationMessage(String.format("잔액이 부족합니다. 필요 금액: %d원, 현재 잔액: %d원", menuPrice, userBalance))
                .build();
    }
    
    // 메뉴 사용 불가 응답 생성
    public static PaymentValidationDto menuUnavailable(Long userNo, Long menuId, String menuType,
                                                     String menuName, String reason) {
        return PaymentValidationDto.builder()
                .userNo(userNo)
                .menuId(menuId)
                .menuType(menuType)
                .menuName(menuName)
                .isMenuAvailable(false)
                .errorCode("MENU_UNAVAILABLE")
                .validationMessage("메뉴를 이용할 수 없습니다: " + reason)
                .build();
    }
}
