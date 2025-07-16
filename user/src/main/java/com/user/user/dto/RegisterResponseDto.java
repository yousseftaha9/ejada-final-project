package com.user.user.dto;

public class RegisterResponseDto {
    private Long userId;
    private String username;
    private String message; 
     public RegisterResponseDto(Long userId, String username, String message) {
        this.userId = userId;
        this.username = username;
        this.message = message;
    }
}
