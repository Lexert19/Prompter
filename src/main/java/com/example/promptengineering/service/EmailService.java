package com.example.promptengineering.service;

import com.mailersend.sdk.MailerSend;
import com.mailersend.sdk.MailerSendResponse;
import com.mailersend.sdk.emails.Email;
import com.mailersend.sdk.exceptions.MailerSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.util.concurrent.CompletableFuture;


@Service
public class EmailService {
    private final String domain;
    private final String apiToken;
    private final String fromName;
    private final String fromEmail;
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public EmailService(@Value("${app.domain}") String domain,
                        @Value("${mailersend.api.token}") String apiToken,
                        @Value("${mailersend.from.name:Prompter}") String fromName,
                        @Value("${mailersend.from.email}") String fromEmail) {
        this.domain = domain;
        this.apiToken = apiToken;
        this.fromName = fromName;
        this.fromEmail = fromEmail;
    }

    @Async("taskExecutor")
    public void sendPasswordResetEmail(String email, String token) {
        try {
            logger.debug("Starting to send password reset email to: {}", email);

            MailerSend ms = new MailerSend();
            ms.setToken(apiToken);

            String resetLink = domain + "/auth/reset-password-confirm?token=" + token;
            String textBody = String.format(
                    "Password reset requested. Click the link: %s%nIf you didn't request this, ignore this email.",
                    resetLink);
            String htmlBody = String.format("<p>Password reset requested. <a href=\"%s\">Click here to reset your password</a></p><p>%s</p><p>If you didn't request this, ignore this email.</p>",
                    resetLink, resetLink);

            Email emailMessage = new Email();
            emailMessage.setFrom(fromName, fromEmail);
            emailMessage.addRecipient(email, email);
            emailMessage.setSubject("Password Reset Request");
            emailMessage.setPlain(textBody);
            emailMessage.setHtml(htmlBody);

            logger.debug("Sending email with reset link: {}", resetLink);
            MailerSendResponse response = ms.emails().send(emailMessage);

            logger.debug("Email sent successfully to: {}", email);
            CompletableFuture.completedFuture(null);

        } catch (MailerSendException e) {
            logger.error("Failed to send password reset email to: {}", email, e);
            CompletableFuture.failedFuture(e);
        } catch (Exception e) {
            logger.error("Unexpected error while sending email to: {}", email, e);
            CompletableFuture.failedFuture(new RuntimeException("Email sending failed", e));
        }
    }


}