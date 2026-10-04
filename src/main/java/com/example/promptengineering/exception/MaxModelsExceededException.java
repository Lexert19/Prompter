package com.example.promptengineering.exception;

public class MaxModelsExceededException extends RuntimeException {
    public MaxModelsExceededException(int max) {
        super("User cannot have more than " + max + " models.");
    }
}
