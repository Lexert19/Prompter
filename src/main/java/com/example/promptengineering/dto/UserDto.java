package com.example.promptengineering.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private double points;
    private boolean twoFactorEnabled;
    private String twoFactorEmail;
}
