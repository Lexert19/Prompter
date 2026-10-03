package com.example.promptengineering.service;

import com.example.promptengineering.repository.TwoFactorCodeStore;
import java.time.Duration;
import java.util.Optional;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class TwoFactorEmailService {

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private final EmailService emailService;
    private final TwoFactorCodeStore codeStore;

    public TwoFactorEmailService(EmailService emailService,
            TwoFactorCodeStore codeStore) {
        this.emailService = emailService;
        this.codeStore = codeStore;
    }

    public void createAndSendCode(String sessionId, String email) {
        String code = generateCode();
        codeStore.save(sessionId, code, CODE_TTL);
        emailService.sendTwoFactorCode(email, code);
    }

    public boolean verifyCode(String sessionId, String code) {
        Optional<String> stored = codeStore.get(sessionId);
        if (stored.isEmpty()) {
            return false;
        }
        if (stored.get().equals(code)) {
            codeStore.remove(sessionId);
            return true;
        }
        return false;
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
