package com.example.promptengineering.dto;

public record ResetPasswordConfirmRequest(String token, String password,
        String passwordConfirmation) {
}
