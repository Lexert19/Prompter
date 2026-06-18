package com.example.promptengineering.restController;

import com.example.promptengineering.dto.LoginRequest;
import com.example.promptengineering.dto.RegisterRequest;
import com.example.promptengineering.dto.ResetPasswordConfirmRequest;
import com.example.promptengineering.dto.ResetPasswordRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.example.promptengineering.entity.ResetToken;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.ResetTokenRepository;
import com.example.promptengineering.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class AuthControllerTest {

    @Autowired
    private ResetTokenRepository resetTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;
    private User user;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String login = "testuser123@wp.pl";
        String password = "testpassword123";

        User existingUser = userRepository.findByEmail(login).orElse(null);
        if (existingUser != null) {
            existingUser.setPassword(passwordEncoder.encode(password));
            userRepository.save(existingUser);
            user = existingUser;
        } else {
            User newUser = new User();
            newUser.setEmail(login);
            newUser.setPassword(passwordEncoder.encode(password));
            userRepository.save(newUser);
            user = newUser;
        }
    }

    @Test
    @Transactional
    public void testRegisterSuccess() throws Exception {
        String newEmail = "newuser@wp.pl";
        userRepository.findByEmail(newEmail).ifPresent(userRepository::delete);

        RegisterRequest registerRequest = new RegisterRequest(newEmail,
                "securepassword123");

        mockMvc.perform(post("/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        Optional<User> registeredUser = userRepository.findByEmail(newEmail);
        assertTrue(registeredUser.isPresent());
    }

    @Test
    public void testRegisterFailureEmailExists() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest("testuser123@wp.pl",
                "anotherpassword");

        mockMvc.perform(post("/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest("testuser123@wp.pl",
                "testpassword123");

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
        LoginRequest loginRequest = new LoginRequest("testuser123@wp.pl",
                "wrongpassword");

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void testPasswordResetRequest() throws Exception {
        resetTokenRepository.deleteByUserLogin("testuser123@wp.pl");

        ResetPasswordRequest resetRequest = new ResetPasswordRequest("testuser123@wp.pl");

        mockMvc.perform(post("/auth/reset-password-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(resetRequest)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message")
                        .value("If the email exists, a reset link has been sent."));

        List<ResetToken> resetTokens = resetTokenRepository
                .findByUserLogin("testuser123@wp.pl");
        assertFalse(resetTokens.isEmpty(), "Reset token should have been created");
    }

    @Test
    @Transactional
    public void testPasswordResetConfirmationSuccess() throws Exception {
        String uniqueToken = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken();
        resetToken.setToken(uniqueToken);
        resetToken.setUserLogin("testuser123@wp.pl");
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

        Optional<User> updatedUser = userRepository
                .findByEmail(resetToken.getUserLogin());
        assertFalse(updatedUser.isEmpty(), "User should exist");
        assertTrue(
                passwordEncoder.matches("nowe_haslo", updatedUser.get().getPassword()));

        updatedUser.get().setPassword(passwordEncoder.encode("testpassword123"));
        userRepository.save(updatedUser.get());
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
        resetToken.setUserLogin("testuser123@wp.pl");
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
