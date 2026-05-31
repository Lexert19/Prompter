package com.example.promptengineering.dto;

public record JwtResponse(String token) {
  public String getToken() {
    return token;
  }
}
