package com.example.promptengineering.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    String email,

    @NotBlank(message = "Password cannot be empty")
    String password
) {}
