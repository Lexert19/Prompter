package com.example.promptengineering.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceIntegrationTest {

    @Autowired
    private EmailService emailService;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger emailServiceLogger;

    @BeforeEach
    void setUp() {
        emailServiceLogger = (Logger) LoggerFactory.getLogger(EmailService.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        emailServiceLogger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        emailServiceLogger.detachAppender(listAppender);
    }

    @Test
    void shouldSendPasswordResetEmailWithoutErrors() throws Exception {
        String toEmail = "d0mi1@wp.pl";
        String token = "test-token-123";

        emailService.sendPasswordResetEmail(toEmail, token);

        Thread.sleep(2000);

        assertThat(listAppender.list).noneMatch(event -> event.getLevel() == Level.ERROR
                && event.getFormattedMessage().contains("Failed to send password reset email"));
    }

    @Test
    void shouldSendTwoFactorCodeWithoutErrors() throws Exception {
        String toEmail = "d0mi1@wp.pl";
        String code = "123456";

        emailService.sendTwoFactorCode(toEmail, code);

        Thread.sleep(2000);

        assertThat(listAppender.list).noneMatch(event -> event.getLevel() == Level.ERROR
                && event.getFormattedMessage().contains("Failed to send 2FA code"));
    }
}
