package com.example.promptengineering.restController;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.entity.AuditLog;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.ActionType;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.model.ResultType;
import com.example.promptengineering.repository.AuditLogRepository;
import com.example.promptengineering.repository.SharedKeyRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.AuditLogService;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuditIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private SharedKeyRepository sharedKeyRepository;

    private User adminUser;
    private User normalUser;

    private String adminEmail;
    private String userEmail;

    @BeforeEach
    void setUp() throws Exception {
        adminEmail = "admin-" + UUID.randomUUID() + "@audit.com";
        userEmail = "user-" + UUID.randomUUID() + "@audit.com";

        adminUser = userService.createUser(adminEmail, "adminPass",
                List.of(AppRole.ADMIN));
        normalUser = userService.createUser(userEmail, "userPass", List.of(AppRole.USER));
    }

    @AfterEach
    void tearDown() {
        sharedKeyRepository.deleteAll();

        auditLogRepository.deleteAll();

        if (adminUser != null && adminUser.getId() != null) {
            userRepository.delete(adminUser);
        }
        if (normalUser != null && normalUser.getId() != null) {
            userRepository.delete(normalUser);
        }
    }

    @Test
    void shouldLogLoginSuccess() throws Exception {
        String loginJson = objectMapper
                .writeValueAsString(Map.of("email", adminEmail, "password", "adminPass"));

        mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)).andExpect(status().isOk());

        List<AuditLog> logs = auditLogRepository.findByUserIdAndAction(adminUser.getId(),
                ActionType.LOGIN_SUCCESS);
        assertThat(logs).hasSize(1);
        AuditLog log = logs.getFirst();
        assertThat(log.getResult()).isEqualTo(ResultType.SUCCESS);
        assertThat(log.getUsername()).isEqualTo(adminEmail);
    }

    @Test
    void shouldLogRoleChangeWhenAdminChangesUserRole() throws Exception {
        mockMvc.perform(post("/admin/users/{id}/role", normalUser.getId())
                .param("role", "ADMIN").with(csrf())
                .with(user(userService.loadUserByUsername(adminEmail))))
                .andExpect(status().is3xxRedirection());

        List<AuditLog> logs = auditLogRepository.findByUserIdAndAction(adminUser.getId(),
                ActionType.ROLE_UPDATE);
        assertThat(logs).hasSize(1);
        AuditLog log = logs.getFirst();
        assertThat(log.getResult()).isEqualTo(ResultType.SUCCESS);
        assertThat(log.getTarget()).contains(normalUser.getId().toString());
    }

    @Test
    void shouldLogUserDeleteWhenAdminDeletesUser() throws Exception {
        mockMvc.perform(post("/admin/users/{id}/delete", normalUser.getId()).with(csrf())
                .with(user(userService.loadUserByUsername(adminEmail))))
                .andExpect(status().is3xxRedirection());

        List<AuditLog> logs = auditLogRepository.findByUserIdAndAction(adminUser.getId(),
                ActionType.USER_DELETE);
        assertThat(logs).hasSize(1);
        AuditLog log = logs.getFirst();
        assertThat(log.getResult()).isEqualTo(ResultType.SUCCESS);
        assertThat(log.getTarget()).contains(normalUser.getId().toString());
    }

    @Test
    void shouldLogFailureWhenAdminDeletesSelf() throws Exception {
        mockMvc.perform(post("/admin/users/{id}/delete", adminUser.getId()).with(csrf())
                .with(user(userService.loadUserByUsername(adminEmail))))
                .andExpect(status().is3xxRedirection());

        List<AuditLog> logs = auditLogRepository.findByUserIdAndAction(adminUser.getId(),
                ActionType.USER_DELETE);
        assertThat(logs).hasSize(1);
        AuditLog log = logs.getFirst();
        assertThat(log.getResult()).isEqualTo(ResultType.FAILURE);
        assertThat(log.getDetails()).contains("cannot delete your own account");
    }

    @Test
    void shouldLogSharedKeyAddWhenAdminAddsKey() throws Exception {
        String keyJson = objectMapper.writeValueAsString(
                Map.of("provider", "OPENAI", "keyValue", "sk-test123"));

        mockMvc.perform(post("/api/admin/shared-keys")
                .contentType(MediaType.APPLICATION_JSON).content(keyJson).with(csrf())
                .with(user(userService.loadUserByUsername(adminEmail))))
                .andExpect(status().isOk());

        List<AuditLog> logs = auditLogRepository.findByUserIdAndAction(adminUser.getId(),
                ActionType.SHARED_KEY_GENERATE);
        assertThat(logs).hasSize(1);
        AuditLog log = logs.getFirst();
        assertThat(log.getResult()).isEqualTo(ResultType.SUCCESS);
        assertThat(log.getTarget()).contains("OPENAI");
    }

    @Test
    void shouldLogSharedKeyDeleteWhenAdminDeletesKey() throws Exception {
        String keyJson = objectMapper.writeValueAsString(
                Map.of("provider", "ANTHROPIC", "keyValue", "sk-anthropic"));
        String response = mockMvc
                .perform(post("/api/admin/shared-keys")
                        .contentType(MediaType.APPLICATION_JSON).content(keyJson)
                        .with(csrf())
                        .with(user(userService.loadUserByUsername(adminEmail))))
                .andExpect(status().isOk()).andReturn().getResponse()
                .getContentAsString();

        Long keyId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(delete("/api/admin/shared-keys/{id}", keyId)
                .contentType(MediaType.APPLICATION_JSON).content("{}").with(csrf())
                .with(user(userService.loadUserByUsername(adminEmail))))
                .andExpect(status().isNoContent());

        List<AuditLog> logs = auditLogRepository.findByUserIdAndAction(adminUser.getId(),
                ActionType.SHARED_KEY_DELETE);
        assertThat(logs).hasSize(1);
        AuditLog log = logs.getFirst();
        assertThat(log.getResult()).isEqualTo(ResultType.SUCCESS);
        assertThat(log.getTarget()).contains(keyId.toString());
    }

    @Test
    void shouldFilterByUserIdAndAction() throws Exception {
        auditLogService.log(auditLogService.createAuditLog(adminUser.getId(), adminEmail,
                ActionType.LOGIN_SUCCESS, ResultType.SUCCESS, null, null, null));

        auditLogService.log(auditLogService.createAuditLog(normalUser.getId(), userEmail,
                ActionType.LOGIN_FAILURE, ResultType.FAILURE, null, null, null));

        mockMvc.perform(
                get("/api/admin/audit/logs").param("userId", adminUser.getId().toString())
                        .param("action", "LOGIN_SUCCESS")
                        .with(user(userService.loadUserByUsername(adminEmail))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].username").value(adminEmail));
    }
}
