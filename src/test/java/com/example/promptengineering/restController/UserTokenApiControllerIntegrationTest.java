package com.example.promptengineering.restController;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class UserTokenApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() throws Exception {
        adminUser = userService.createUser("admin@tokens.com", "password",
                List.of(AppRole.ADMIN));

        normalUser = userService.createUser("user@tokens.com", "password",
                List.of(AppRole.USER));
    }

    @Test
    void generateToken_asAdmin_shouldWork() throws Exception {
        mockMvc.perform(
                post("/api/admin/users/{id}/token", normalUser.getId()).with(csrf())
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiToken", matchesPattern("[A-Za-z0-9_-]{40,}")));

        assertThat(userRepository.findById(normalUser.getId()).get().getApiToken())
                .isNotBlank();
    }

    @Test
    void generateToken_asUserOnAdminEndpoint_shouldReturnForbidden() throws Exception {
        mockMvc.perform(
                post("/api/admin/users/{id}/token", normalUser.getId()).with(csrf()).with(
                        user(userService.loadUserByUsername(normalUser.getEmail()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void generateToken_asUserForSelf_shouldWork() throws Exception {
        mockMvc.perform(post("/api/users/me/token").with(csrf())
                .with(user(userService.loadUserByUsername(normalUser.getEmail()))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.apiToken").exists());
    }
}
