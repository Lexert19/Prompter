package com.example.promptengineering.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateProjectDTO(
        @NotBlank(message = "name must not be blank") @Size(max = 100, message = "name max 100 chars") String name) {
}
