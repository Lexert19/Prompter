package com.example.promptengineering.controller;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.EmailService;
import com.example.promptengineering.service.TwoFactorEmailService;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class TwoFactorSetupControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TwoFactorEmailService twoFactorEmailService;

    @MockBean
    private EmailService emailService;

    private User testUser;
    private RequestPostProcessor asTestUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("test@example.com")
                .orElseGet(() -> {
                    User u = new User();
                    u.setEmail("test@example.com");
                    u.setPassword(passwordEncoder.encode("password"));
                    u.setTwoFactorEnabled(false);
                    u.setTwoFactorEmail(null);
                    u.setEncryptedKeys("some-keys");
                    return userRepository.save(u);
                });
        asTestUser = user(userService.loadUserByUsername(testUser.getEmail()));
    }

    @Test
    void sendTestCode_shouldCallEmailService() throws Exception {
        doNothing().when(emailService).sendTwoFactorCode(anyString(), anyString());

        mockMvc.perform(post("/api/2fa/send-test")
                        .param("email", "test@example.com")
                        .with(asTestUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Kod wysłany na test@example.com"));

        verify(emailService).sendTwoFactorCode(eq("test@example.com"), anyString());
    }

    @Test
    void enableTwoFactor_withValidCode_shouldEnable2FA() throws Exception {
        String email = "test@example.com";

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(emailService).sendTwoFactorCode(eq(email), codeCaptor.capture());

        mockMvc.perform(post("/api/2fa/send-test")
                        .param("email", email)
                        .with(asTestUser))
                .andExpect(status().isOk());

        String actualCode = codeCaptor.getValue();

        mockMvc.perform(post("/api/2fa/enable")
                        .param("email", email)
                        .param("code", actualCode)
                        .with(asTestUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assert updated.isTwoFactorEnabled();
        assert updated.getTwoFactorEmail().equals(email);
    }

    @Test
    void enableTwoFactor_withInvalidCode_shouldReturnBadRequest() throws Exception {
        String email = "test@example.com";
        String wrongCode = "000000";

        mockMvc.perform(post("/api/2fa/enable")
                        .param("email", email)
                        .param("code", wrongCode)
                        .with(asTestUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Nieprawidłowy kod"));

        User unchanged = userRepository.findById(testUser.getId()).orElseThrow();
        assert !unchanged.isTwoFactorEnabled();
        assert unchanged.getTwoFactorEmail() == null;
    }

    @Test
    void disableTwoFactor_withValidCode_shouldDisable2FA() throws Exception {
        testUser.setTwoFactorEnabled(true);
        testUser.setTwoFactorEmail("test@example.com");
        userRepository.save(testUser);

        String email = "test@example.com";

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(emailService).sendTwoFactorCode(eq(email), codeCaptor.capture());

        mockMvc.perform(post("/api/2fa/send-test")
                        .param("email", email)
                        .with(asTestUser))
                .andExpect(status().isOk());

        String actualCode = codeCaptor.getValue();

        mockMvc.perform(post("/api/2fa/disable")
                        .param("code", actualCode)
                        .with(asTestUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assert !updated.isTwoFactorEnabled();
        assert updated.getTwoFactorEmail() == null;
    }

    @Test
    void disableTwoFactor_withInvalidCode_shouldReturnBadRequest() throws Exception {
        testUser.setTwoFactorEnabled(true);
        testUser.setTwoFactorEmail("test@example.com");
        userRepository.save(testUser);

        String wrongCode = "000000";

        mockMvc.perform(post("/api/2fa/disable")
                        .param("code", wrongCode)
                        .with(asTestUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Nieprawidłowy kod"));

        User unchanged = userRepository.findById(testUser.getId()).orElseThrow();
        assert unchanged.isTwoFactorEnabled();
        assert unchanged.getTwoFactorEmail() != null;
    }
}