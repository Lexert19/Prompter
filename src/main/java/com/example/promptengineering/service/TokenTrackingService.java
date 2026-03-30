package com.example.promptengineering.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TokenTrackingService {
    public int getCompletionTokens() {
        return 2000;
    }
}
