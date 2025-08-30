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
    
    // 포인트 사용 관련 필드 추가
    private Boolean usePoints;           // 포인트 사용 여부
    private Integer pointAmount;         // 사용할 포인트 금액
    private Integer finalAmount;         // 포인트 차감 후 최종 결제 금액
    private Boolean isPointSufficient;   // 포인트 충분성 여부
    private Integer availablePoints;     // 사용 가능한 포인트
    
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
                .usePoints(false)
                .pointAmount(0)
                .finalAmount(menuPrice)
                .isPointSufficient(true)
                .availablePoints(0)
                .build();
    }
    
    // 포인트 사용 시 검증 성공 응답 생성
    public static PaymentValidationDto successWithPoints(Long userNo, Long menuId, String menuType, 
                                                       String menuName, Integer menuPrice, Integer userBalance,
                                                       Integer pointAmount, Integer finalAmount, Integer availablePoints) {
        return PaymentValidationDto.builder()
                .userNo(userNo)
                .menuId(menuId)
                .menuType(menuType)
                .menuName(menuName)
                .menuPrice(menuPrice)
                .userBalance(userBalance)
                .isBalanceSufficient(userBalance >= finalAmount)
                .isMenuAvailable(true)
                .validationMessage("포인트 사용 결제 검증이 완료되었습니다")
                .usePoints(true)
                .pointAmount(pointAmount)
                .finalAmount(finalAmount)
                .isPointSufficient(true)
                .availablePoints(availablePoints)
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
    
    // 포인트 부족 응답 생성
    public static PaymentValidationDto insufficientPoints(Long userNo, Long menuId, String menuType,
                                                        String menuName, Integer menuPrice, Integer availablePoints, Integer requestedPoints) {
        return PaymentValidationDto.builder()
                .userNo(userNo)
                .menuId(menuId)
                .menuType(menuType)
                .menuName(menuName)
                .menuPrice(menuPrice)
                .isBalanceSufficient(false)
                .isMenuAvailable(true)
                .errorCode("INSUFFICIENT_POINTS")
                .validationMessage(String.format("포인트가 부족합니다. 요청 포인트: %d, 현재 포인트: %d", requestedPoints, availablePoints))
                .usePoints(true)
                .pointAmount(requestedPoints)
                .isPointSufficient(false)
                .availablePoints(availablePoints)
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
