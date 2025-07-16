package com.user.user.dto;

public class RegisterErrorResponse {
      private String error;
    private String message;
    private int status;
      public RegisterErrorResponse(String error, String message, int status) {
        this.error = error;
        this.message = message;
        this.status = status;
    }
}
