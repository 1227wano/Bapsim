package com.bapsim.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PinRegistrationDto {
    
    @NotNull(message = "사용자 번호는 필수입니다")
    private Long userNo;
    
    // PIN 등록 시에는 null, PIN 수정 시에는 4자리 숫자
    @Size(min = 4, max = 4, message = "현재 PIN은 4자리여야 합니다")
    @Pattern(regexp = "^[0-9]{4}$", message = "PIN은 숫자 4자리여야 합니다")
    private String currentPin;
    
    @NotNull(message = "새로운 PIN은 필수입니다")
    @Size(min = 4, max = 4, message = "새로운 PIN은 4자리여야 합니다")
    @Pattern(regexp = "^[0-9]{4}$", message = "PIN은 숫자 4자리여야 합니다")
    private String newPin;
    
    @NotNull(message = "새로운 PIN 확인은 필수입니다")
    @Size(min = 4, max = 4, message = "새로운 PIN 확인은 4자리여야 합니다")
    @Pattern(regexp = "^[0-9]{4}$", message = "PIN 확인은 숫자 4자리여야 합니다")
    private String confirmPin;
    
    // PIN 등록 시 사용 (현재 PIN이 없는 경우)
    public static PinRegistrationDto forInitialRegistration(Long userNo, String newPin, String confirmPin) {
        PinRegistrationDto dto = new PinRegistrationDto();
        dto.setUserNo(userNo);
        dto.setNewPin(newPin);
        dto.setConfirmPin(confirmPin);
        return dto;
    }
    
    // PIN 수정 시 사용
    public static PinRegistrationDto forUpdate(Long userNo, String currentPin, String newPin, String confirmPin) {
        return new PinRegistrationDto(userNo, currentPin, newPin, confirmPin);
    }
    
    // PIN 확인 일치 여부 검증
    public boolean isPinMatching() {
        return newPin != null && newPin.equals(confirmPin);
    }
    
    // 현재 PIN과 새 PIN이 다른지 검증
    public boolean isPinDifferent() {
        return currentPin != null && !currentPin.equals(newPin);
    }
}
