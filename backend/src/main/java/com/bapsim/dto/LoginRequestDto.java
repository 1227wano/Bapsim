package com.bapsim.dto;

public class LoginRequestDto {
    private String userId;
    private String userPass;

    // 기본 생성자
    public LoginRequestDto() {
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPass() {
        return userPass;
    }

    public void setUserPass(String userPass) {
        this.userPass = userPass;
    }
}
