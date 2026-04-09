package com.example.promptengineering.restController;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.ResetTokenRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ResetTokenRepository resetTokenRepository;

    private final String userEmail = "test@example.com";
    private final String userPassword = "password123";

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setEmail(userEmail);
        testUser.setPassword(passwordEncoder.encode(userPassword));
        userService.setUserKeys(testUser, new HashMap<>());
        userRepository.save(testUser);

        UserDetails userDetails = userService.loadUserByUsername(userEmail);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        SecurityContextHolder.getContext().setAuthentication(authentication);

    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSaveApiKeyForUser() throws Exception {
        String keyName = "OPENAI";
        String keyValue = "sk-1234567890abcdef";

        mockMvc.perform(post("/api/account/save-key/{keyName}", keyName).with(csrf())
                .with(user(userService.loadUserByUsername(userEmail)))
                .contentType(MediaType.TEXT_PLAIN).content(keyValue))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        String.format("Key '%s' saved to map for user with email: %s",
                                keyName, userEmail)));

        User updatedUser = userRepository.findByEmail(userEmail).orElseThrow();
        assert userService.getUserKeys(updatedUser).containsKey(keyName);
        assert userService.getUserKeys(updatedUser).get(keyName).equals(keyValue);
    }

    @Test
    void shouldReturnAllKeysForUser() throws Exception {
        HashMap<String, String> keys = new HashMap<>();
        keys.put("OPENAI", "sk-openai-test");
        keys.put("ANTHROPIC", "sk-anthropic-test");
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        userService.setUserKeys(user, keys);
        userRepository.save(user);

        mockMvc.perform(get("/api/account/keys")
                .with(user(userService.loadUserByUsername(userEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.OPENAI").value("sk-openai-test"))
                .andExpect(jsonPath("$.ANTHROPIC").value("sk-anthropic-test"));
    }

    @Test
    void shouldReturnEmptyMapWhenNoKeysExist() throws Exception {
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        userService.setUserKeys(user, new HashMap<>());
        userRepository.save(user);

        mockMvc.perform(get("/api/account/keys")
                .with(user(userService.loadUserByUsername(userEmail))))
                .andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldOverwriteExistingKey() throws Exception {
        String keyName = "OPENAI";
        String initialKeyValue = "sk-initial";
        String newKeyValue = "sk-updated";

        User user = userRepository.findByEmail(userEmail).orElseThrow();
        userService.getUserKeys(user).put(keyName, initialKeyValue);
        userRepository.save(user);

        mockMvc.perform(post("/api/account/save-key/{keyName}", keyName).with(csrf())
                .with(user(userService.loadUserByUsername(userEmail)))
                .contentType(MediaType.TEXT_PLAIN).content(newKeyValue))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findByEmail(userEmail).orElseThrow();
        assert userService.getUserKeys(updatedUser).get(keyName).equals(newKeyValue);
        assert userService.getUserKeys(updatedUser).size() == 1;
    }

    @Test
    void shouldHandleSpecialCharactersInKeyValue() throws Exception {
        String keyName = "CUSTOM_API";
        String keyValue = "api_key_with!@#$%^&*()_+special_chars";

        mockMvc.perform(post("/api/account/save-key/{keyName}", keyName).with(csrf())
                .with(user(userService.loadUserByUsername(userEmail)))
                .contentType(MediaType.TEXT_PLAIN).content(keyValue))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findByEmail(userEmail).orElseThrow();
        assert userService.getUserKeys(updatedUser).get(keyName).equals(keyValue);
    }

    @Test
    void shouldHandleEmptyKeyValue() throws Exception {
        String keyName = "EMPTY_KEY";

        mockMvc.perform(post("/api/account/save-key/{keyName}", keyName).with(csrf())
                .with(user(userService.loadUserByUsername(userEmail)))
                .contentType(MediaType.TEXT_PLAIN).content("\"\""))
                .andExpect(status().is2xxSuccessful());

        User updatedUser = userRepository.findByEmail(userEmail).orElseThrow();

        // assert userService.getUserKeys(updatedUser).get(keyName).isEmpty();
    }

    @Test
    void shouldReturn401WhenUserNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        String keyName = "OPENAI";
        String keyValue = "sk-test";

        mockMvc.perform(post("/api/account/save-key/{keyName}", keyName).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(keyValue))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/account/keys")).andExpect(status().isUnauthorized());

    }

    @Test
    void shouldPreserveExistingKeysWhenAddingNewOne() throws Exception {
        HashMap<String, String> initialKeys = new HashMap<>();
        initialKeys.put("OPENAI", "sk-openai");
        initialKeys.put("ANTHROPIC", "sk-anthropic");
        User user = userRepository.findByEmail(userEmail).orElseThrow();
        userService.setUserKeys(user, initialKeys);
        userRepository.save(user);

        mockMvc.perform(post("/api/account/save-key/{keyName}", "GEMINI").with(csrf())
                .with(user(userService.loadUserByUsername(userEmail)))
                .contentType(MediaType.TEXT_PLAIN).content("sk-gemini"))
                .andExpect(status().isOk());

        User updatedUser = userRepository.findByEmail(userEmail).orElseThrow();
        assert userService.getUserKeys(updatedUser).size() == 3;
        assert userService.getUserKeys(updatedUser).get("OPENAI").equals("sk-openai");
        assert userService.getUserKeys(updatedUser).get("ANTHROPIC")
                .equals("sk-anthropic");
        assert userService.getUserKeys(updatedUser).get("GEMINI").equals("sk-gemini");
    }
}
