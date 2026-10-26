package com.example.promptengineering.controller;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.component.IpRateLimiter;
import com.example.promptengineering.component.UserActionLimiter;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.EmailService;
import com.example.promptengineering.service.TwoFactorEmailService;
import com.example.promptengineering.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TwoFactorSetupControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TwoFactorEmailService twoFactorEmailService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    protected IpRateLimiter ipRateLimiter;

    @MockitoBean
    protected UserActionLimiter userActionLimiter;

    private User testUser;
    private String testEmail;
    private RequestPostProcessor asTestUser;

    @BeforeEach
    void setUp() {
        when(ipRateLimiter.isAllowed(any())).thenReturn(true);
        when(userActionLimiter.canPerform(any())).thenReturn(true);

        testEmail = "2fa-" + UUID.randomUUID() + "@example.com";

        testUser = new User();
        testUser.setEmail(testEmail);
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser.setTwoFactorEnabled(false);
        testUser.setTwoFactorEmail(null);
        testUser.setEncryptedKeys("some-keys");
        testUser = userRepository.save(testUser);

        asTestUser = user(userService.loadUserByUsername(testEmail));
    }

    @Test
    void sendTestCode_shouldCallEmailService() throws Exception {
        doNothing().when(emailService).sendTwoFactorCode(anyString(), anyString());

        mockMvc.perform(
                post("/api/2fa/send-test").param("email", testEmail).with(asTestUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Code sent to " + testEmail));

        verify(emailService).sendTwoFactorCode(eq(testEmail), anyString());
    }

    @Test
    void enableTwoFactor_withValidCode_shouldEnable2FA() throws Exception {
        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(emailService).sendTwoFactorCode(eq(testEmail),
                codeCaptor.capture());

        mockMvc.perform(
                post("/api/2fa/send-test").param("email", testEmail).with(asTestUser))
                .andExpect(status().isOk());

        String actualCode = codeCaptor.getValue();

        mockMvc.perform(post("/api/2fa/enable").param("email", testEmail)
                .param("code", actualCode).with(asTestUser)).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assert updated.isTwoFactorEnabled();
        assert updated.getTwoFactorEmail().equals(testEmail);
    }

    @Test
    void enableTwoFactor_withInvalidCode_shouldReturnBadRequest() throws Exception {
        String wrongCode = "000000";

        mockMvc.perform(post("/api/2fa/enable").param("email", testEmail)
                .param("code", wrongCode).with(asTestUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid code"));

        User unchanged = userRepository.findById(testUser.getId()).orElseThrow();
        assert !unchanged.isTwoFactorEnabled();
        assert unchanged.getTwoFactorEmail() == null;
    }

    @Test
    void disableTwoFactor_withValidCode_shouldDisable2FA() throws Exception {
        testUser.setTwoFactorEnabled(true);
        testUser.setTwoFactorEmail(testEmail);
        userRepository.save(testUser);

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(emailService).sendTwoFactorCode(eq(testEmail),
                codeCaptor.capture());

        mockMvc.perform(
                post("/api/2fa/send-test").param("email", testEmail).with(asTestUser))
                .andExpect(status().isOk());

        String actualCode = codeCaptor.getValue();

        mockMvc.perform(
                post("/api/2fa/disable").param("code", actualCode).with(asTestUser))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assert !updated.isTwoFactorEnabled();
        assert updated.getTwoFactorEmail() == null;
    }

    @Test
    void disableTwoFactor_withInvalidCode_shouldReturnBadRequest() throws Exception {
        testUser.setTwoFactorEnabled(true);
        testUser.setTwoFactorEmail(testEmail);
        userRepository.save(testUser);

        String wrongCode = "000000";

        mockMvc.perform(
                post("/api/2fa/disable").param("code", wrongCode).with(asTestUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid code"));

        User unchanged = userRepository.findById(testUser.getId()).orElseThrow();
        assert unchanged.isTwoFactorEnabled();
        assert unchanged.getTwoFactorEmail() != null;
    }
}
