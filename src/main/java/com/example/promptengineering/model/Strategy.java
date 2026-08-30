package com.example.promptengineering.model;

public enum Strategy {
  OPENAI, ANTHROPIC, DEFAULT, GEMINI, OPENROUTER, NVIDIA;

  public static Strategy fromString(String s) {
    if (s == null) return OPENAI;
    try {
      return Strategy.valueOf(s.toUpperCase().trim());
    } catch (Exception e) {
      return OPENAI;
    }
  }

  public boolean isDefault() {
    return this == DEFAULT;
  }
}