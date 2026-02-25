package com.example.promptengineering.restController;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@Slf4j
@SpringBootTest
public class AuthControllerTest {


    @Autowired
    private ResetTokenRepository resetTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private WebApplicationContext webApplicationContext;

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
    public void testLoginSuccess() throws Exception {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", "testuser123@wp.pl");
        formData.add("password", "testpassword123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(formData))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"));
    }

    @Test
    public void testShowLoginForm() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Login")));
    }

    @Test
    public void testLoginFailure() throws Exception {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", "testuser123@wp.pl");
        formData.add("password", "wrongpassword");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(formData))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/auth/login?error=true"));
    }


    @Test
    @Transactional
    public void testPasswordResetRequest() throws Exception {
        resetTokenRepository.deleteByUserLogin("testuser123@wp.pl");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", "testuser123@wp.pl");

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        MvcResult result = mockMvc.perform(post("/auth/reset-password-request")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(formData))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        log.info("Response body: {}", result.getResponse().getContentAsString());

        List<ResetToken> resetTokens = resetTokenRepository.findByUserLogin("testuser123@wp.pl");
        assertFalse(resetTokens.isEmpty(), "Lista tokenów resetowania hasła jest pusta");
    }


    @Test
    public void testPasswordResetConfirmationSuccess() throws Exception {
        String uniqueToken = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken();
        resetToken.setToken(uniqueToken);
        resetToken.setUserLogin("testuser123@wp.pl");
        resetToken.setCreationTime(LocalDateTime.now());
        resetToken.setUser(user);
        resetTokenRepository.save(resetToken);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", resetToken.getToken());
        formData.add("password", "nowe_haslo");
        formData.add("password_confirmation", "nowe_haslo");

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        mockMvc.perform(post("/auth/reset-password-confirm")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(formData))
                .andExpect(status().is2xxSuccessful());

        Optional<User> user = userRepository.findByEmail(resetToken.getUserLogin());
        assertFalse(user.isEmpty(), "User not found");

        assertTrue(passwordEncoder.matches("nowe_haslo", user.get().getPassword()));

        user.get().setPassword(passwordEncoder.encode("testpassword123"));
        userRepository.save(user.get());
    }


    @Test
    public void testPasswordResetConfirmationFailure() throws Exception {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", "invalid_token");
        formData.add("password", "nowe_haslo");
        formData.add("password_confirmation", "nowe_haslo");

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        mockMvc.perform(post("/auth/reset-password-confirm")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(formData))
                .andExpect(status().is2xxSuccessful());
    }



    @Test
    public void testExpiredResetToken() throws Exception {
        String id = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken();
        resetToken.setToken(id);
        resetToken.setUserLogin("testuser123@wp.pl");
        resetToken.setUser(user);
        resetToken.setCreationTime(LocalDateTime.now().minusHours(2));
        resetTokenRepository.save(resetToken);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", id);
        formData.add("password", "nowe_haslo");
        formData.add("password_confirmation", "nowe_haslo");

        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        mockMvc.perform(post("/auth/reset-password-confirm")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .params(formData))
                .andExpect(status().isOk());
    }

}
