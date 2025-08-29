package com.bapsim.dto;

public class LoginResponseDto {
    private String userName;

    public LoginResponseDto(String userName) {
        this.userName = userName;
    }

    // Getter and Setter
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
