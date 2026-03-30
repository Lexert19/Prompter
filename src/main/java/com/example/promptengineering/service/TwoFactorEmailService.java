package com.example.promptengineering.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TwoFactorEmailService {

    private final EmailService emailService;
    private final Map<String, TwoFactorCode> codeStore = new ConcurrentHashMap<>();

    public TwoFactorEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    private static class TwoFactorCode {
        String code;
        LocalDateTime expiry;
        TwoFactorCode(String code) {
            this.code = code;
            this.expiry = LocalDateTime.now().plusMinutes(10);
        }
        boolean isValid() {
            return LocalDateTime.now().isBefore(expiry);
        }
    }

    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    public void createAndSendCode(String sessionId, String email) {
        String code = generateCode();
        codeStore.put(sessionId, new TwoFactorCode(code));
        emailService.sendTwoFactorCode(email, code);
    }

    public boolean verifyCode(String sessionId, String code) {
        TwoFactorCode stored = codeStore.get(sessionId);
        if (stored == null)
            return false;
        if (!stored.isValid()) {
            codeStore.remove(sessionId);
            return false;
        }
        if (stored.code.equals(code)) {
            codeStore.remove(sessionId);
            return true;
        }
        return false;
    }
}
