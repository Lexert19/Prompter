package com.example.promptengineering.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class EmailService {

//    @Autowired
//    private JavaMailSender javaMailSender;
//    @Value("${app.domain}")
//    private String domain;


    public Mono<Void> sendPasswordResetEmail(String email, String token) {
        return Mono.fromRunnable(() -> {
            try {
//                MimeMessage message = javaMailSender.createMimeMessage();
//                MimeMessageHelper helper = new MimeMessageHelper(message);
//
//                String resetLink = domain + "/auth/reset-password-confirm?token=" + token;
//                String textBody = String.format(
//                        "Password reset requested. Click the link: %s%nIf you didn't request this, ignore this email.",
//                        resetLink);
//
//                helper.setTo(email);
//                helper.setSubject("Password Reset Request");
//                helper.setText(textBody);
//
//                javaMailSender.send(message);
            } catch (Exception e) {
                throw new RuntimeException("Email sending failed", e);
            }
        })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}