package com.bapsim.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinRegistrationResponseDto {
    
    private boolean success;
    private String message;
    private String errorCode;
    private Long userNo;
    private boolean isPinRegistered;
    
    // 성공 응답 생성
    public static PinRegistrationResponseDto success(Long userNo, String message) {
        return PinRegistrationResponseDto.builder()
                .success(true)
                .message(message)
                .userNo(userNo)
                .isPinRegistered(true)
                .build();
    }
    
    // PIN 등록 성공 응답
    public static PinRegistrationResponseDto pinRegistered(Long userNo) {
        return success(userNo, "PIN이 성공적으로 등록되었습니다");
    }
    
    // PIN 수정 성공 응답
    public static PinRegistrationResponseDto pinUpdated(Long userNo) {
        return success(userNo, "PIN이 성공적으로 수정되었습니다");
    }
    
    // 실패 응답 생성
    public static PinRegistrationResponseDto failure(String errorCode, String message) {
        return PinRegistrationResponseDto.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .build();
    }
    
    // PIN 불일치 응답
    public static PinRegistrationResponseDto pinMismatch() {
        return failure("PIN_MISMATCH", "새로운 PIN과 확인 PIN이 일치하지 않습니다");
    }
    
    // 현재 PIN 오류 응답
    public static PinRegistrationResponseDto currentPinError() {
        return failure("CURRENT_PIN_ERROR", "현재 PIN이 올바르지 않습니다");
    }
    
    // PIN 변경 없음 응답
    public static PinRegistrationResponseDto noPinChange() {
        return failure("NO_PIN_CHANGE", "새로운 PIN이 현재 PIN과 동일합니다");
    }
    
    // 사용자 없음 응답
    public static PinRegistrationResponseDto userNotFound() {
        return failure("USER_NOT_FOUND", "사용자를 찾을 수 없습니다");
    }
    
    // PIN 형식 오류 응답
    public static PinRegistrationResponseDto invalidPinFormat() {
        return failure("INVALID_PIN_FORMAT", "PIN은 숫자 4자리여야 합니다");
    }
}
