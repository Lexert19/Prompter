package com.example.promptengineering.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("taskExecutor")
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String resetLink = "https://dominik-chyziak.pl/auth/reset-password-confirm?token=" + token;
            String html = "<p>Password reset requested. <a href=\"" + resetLink + "\">Click here to reset your password</a></p>" +
                    "<p>" + resetLink + "</p><p>If you didn't request this, ignore this email.</p>";

            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request");
            helper.setText(html, true);

            mailSender.send(mime);
            logger.debug("Password reset email sent to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send password reset email to {}", toEmail, e);
        }
    }

    @Async("taskExecutor")
    public void sendTwoFactorCode(String toEmail, String code) {
        try {
            String subject = "Twój kod logowania";
            String html = "<p>Twój kod do logowania: <strong>" + code + "</strong></p><p>Kod ważny jest 10 minut.</p>";

            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(mime);
            logger.debug("2FA code sent to {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send 2FA code to {}", toEmail, e);
        }
    }
}