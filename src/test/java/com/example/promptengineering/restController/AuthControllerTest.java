package com.example.promptengineering.restController;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.dto.LoginRequest;
import com.example.promptengineering.dto.RegisterRequest;
import com.example.promptengineering.dto.ResetPasswordConfirmRequest;
import com.example.promptengineering.dto.ResetPasswordRequest;
import com.example.promptengineering.entity.ResetToken;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.ResetTokenRepository;
import com.example.promptengineering.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class AuthControllerTest extends BaseIntegrationTest {

    @Autowired
    private ResetTokenRepository resetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private String userEmail;
    private final String userPassword = "testpassword123";

    @BeforeEach
    public void setup() {
        userEmail = "auth-" + UUID.randomUUID() + "@wp.pl";

        user = new User();
        user.setEmail(userEmail);
        user.setPassword(passwordEncoder.encode(userPassword));
        user = userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        resetTokenRepository.deleteAll(resetTokenRepository.findByUserLogin(userEmail));
        if (user != null && user.getId() != null) {
            userRepository.delete(user);
        }
    }

    @Test
    public void testRegisterFailureEmailExists() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(userEmail,
                "anotherpassword");

        mockMvc.perform(post("/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest(userEmail, userPassword);

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").exists());
    }

    @Test
    public void testShowLoginForm() throws Exception {
        mockMvc.perform(get("/auth/login").with(csrf())).andExpect(status().isOk())
                .andExpect(
                        content().string(org.hamcrest.Matchers.containsString("Login")));
    }

    @Test
    public void testLoginFailure() throws Exception {
        LoginRequest loginRequest = new LoginRequest(userEmail, "wrongpassword");

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testPasswordResetRequest() throws Exception {
        ResetPasswordRequest resetRequest = new ResetPasswordRequest(userEmail);

        mockMvc.perform(post("/auth/reset-password-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message")
                        .value("If the email exists, a reset link has been sent."));

        List<ResetToken> resetTokens = resetTokenRepository.findByUserLogin(userEmail);
        assertFalse(resetTokens.isEmpty(), "Reset token should have been created");
    }

    @Test
    public void testPasswordResetConfirmationSuccess() throws Exception {
        String uniqueToken = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken();
        resetToken.setToken(uniqueToken);
        resetToken.setUserLogin(userEmail);
        resetToken.setCreationTime(LocalDateTime.now());
        resetToken.setUser(user);
        resetTokenRepository.save(resetToken);

        ResetPasswordConfirmRequest confirmRequest = new ResetPasswordConfirmRequest(
                resetToken.getToken(), "nowe_haslo", "nowe_haslo");

        mockMvc.perform(post("/auth/reset-password-confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message")
                        .value("Password has been reset successfully."));

        Optional<User> updatedUser = userRepository.findByEmail(userEmail);
        assertFalse(updatedUser.isEmpty(), "User should exist");
        assertTrue(
                passwordEncoder.matches("nowe_haslo", updatedUser.get().getPassword()));

    }

    @Test
    public void testPasswordResetConfirmationFailure() throws Exception {
        ResetPasswordConfirmRequest confirmRequest = new ResetPasswordConfirmRequest(
                "invalid_token", "nowe_haslo", "nowe_haslo");

        mockMvc.perform(post("/auth/reset-password-confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    public void testExpiredResetToken() throws Exception {
        String expiredToken = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken();
        resetToken.setToken(expiredToken);
        resetToken.setUserLogin(userEmail);
        resetToken.setUser(user);
        resetToken.setCreationTime(LocalDateTime.now().minusHours(2));
        resetTokenRepository.save(resetToken);

        ResetPasswordConfirmRequest confirmRequest = new ResetPasswordConfirmRequest(
                expiredToken, "nowe_haslo", "nowe_haslo");

        mockMvc.perform(post("/auth/reset-password-confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
