package com.example.promptengineering.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PasswordChangeRequest {
    private String newPassword;
    private String confirmPassword;

}
