package com.example.promptengineering.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.promptengineering.entity.ResetToken;
import com.example.promptengineering.repository.ResetTokenRepository;

import reactor.core.publisher.Mono;

@Service
public class ResetTokenService {

    @Autowired
    private AuthService authService;
    private static final int TOKEN_EXPIRATION_HOURS = 24;
    @Autowired
    private ResetTokenRepository resetTokenRepository;
    @Autowired
    private EmailService emailService;



    public Mono<String> createPasswordResetToken(String email) {
        String token = generateUniqueToken();
        ResetToken resetToken = new ResetToken();
        resetToken.setToken(token);
        resetToken.setUserLogin(email);
        resetToken.setCreationTime(LocalDateTime.now());

        return resetTokenRepository.save(resetToken)
                .then(emailService.sendPasswordResetEmail(email, token))
                .thenReturn(token);
    }

    public Mono<ResetToken> validateResetToken(String token) {
        return resetTokenRepository.findByToken(token)
                .switchIfEmpty(Mono.error(new Exception("Invalid reset token")))
                .flatMap(this::validateTokenExpiration);
    }

    public Mono<Void> deleteToken(String token) {
        return resetTokenRepository.deleteByToken(token);
    }

    private String generateUniqueToken() {
        return UUID.randomUUID().toString();
    }

    private Mono<ResetToken> validateTokenExpiration(ResetToken token) {
        LocalDateTime expirationTime = token.getCreationTime().plusHours(TOKEN_EXPIRATION_HOURS);
        return LocalDateTime.now().isBefore(expirationTime)
                ? Mono.just(token)
                : Mono.error(new Exception("Reset token has expired"));
    }

    public Mono<Void> resetPassword(String token, String newPassword) {
        return validateResetToken(token)
                .flatMap(resetToken -> authService.updatePassword(resetToken.getUserLogin(), newPassword))
                .then(deleteToken(token));
    }
}