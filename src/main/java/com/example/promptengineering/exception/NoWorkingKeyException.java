package com.example.promptengineering.exception;

public class NoWorkingKeyException extends RuntimeException {
    private final String provider;

    public NoWorkingKeyException(String provider) {
        super("No working keys for provider: " + provider);
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
