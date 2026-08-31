package com.example.promptengineering.restController;

import com.example.promptengineering.entity.AuditLog;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.ActionType;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.model.ResultType;
import com.example.promptengineering.repository.AuditLogRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuditIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

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

  private User adminUser;
  private User normalUser;

  @BeforeEach
  void setUp() throws Exception {
    userRepository.findByEmail("admin@audit.com").ifPresent(u -> userRepository.delete(u));
    userRepository.findByEmail("user@audit.com").ifPresent(u -> userRepository.delete(u));
    userRepository.findByEmail("testuser@audit.com").ifPresent(u -> userRepository.delete(u));

    adminUser = userService.createUser("admin@audit.com", "adminPass", List.of(AppRole.ADMIN));
    normalUser = userService.createUser("user@audit.com", "userPass", List.of(AppRole.USER));
  }

  @Test
  void shouldLogLoginSuccess() throws Exception {
    String loginJson = objectMapper.writeValueAsString(
        Map.of("email", "admin@audit.com", "password", "adminPass")
    );

    mockMvc.perform(post("/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginJson))
        .andExpect(status().isOk());

    Thread.sleep(500);

    List<AuditLog> logs = auditLogRepository.findByUserIdAndAction(adminUser.getId(), ActionType.LOGIN_SUCCESS);
    assertThat(logs).hasSize(1);
    AuditLog log = logs.get(0);
    assertThat(log.getResult()).isEqualTo(ResultType.SUCCESS);
    assertThat(log.getUsername()).isEqualTo("admin@audit.com");
  }

  @Test
  void shouldLogRoleChangeWhenAdminChangesUserRole() throws Exception {
    mockMvc.perform(post("/admin/users/{id}/role", normalUser.getId())
            .param("role", "ADMIN")
            .with(csrf())
            .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
        .andExpect(status().is3xxRedirection());

    Thread.sleep(500);

    List<AuditLog> logs = auditLogRepository.findByUserIdAndAction(adminUser.getId(), ActionType.ROLE_UPDATE);
    assertThat(logs).hasSize(1);
    AuditLog log = logs.get(0);
    assertThat(log.getResult()).isEqualTo(ResultType.SUCCESS);
    assertThat(log.getTarget()).contains(normalUser.getId().toString());
  }

  @Test
  void shouldLogUserDeleteWhenAdminDeletesUser() throws Exception {
    mockMvc.perform(post("/admin/users/{id}/delete", normalUser.getId())
            .with(csrf())
            .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
        .andExpect(status().is3xxRedirection());

    Thread.sleep(500);

    List<AuditLog> logs = auditLogRepository.findByUserIdAndAction(adminUser.getId(), ActionType.USER_DELETE);
    assertThat(logs).hasSize(1);
    AuditLog log = logs.get(0);
    assertThat(log.getResult()).isEqualTo(ResultType.SUCCESS);
    assertThat(log.getTarget()).contains(normalUser.getId().toString());
  }

  @Test
  void shouldLogFailureWhenAdminDeletesSelf() throws Exception {
    mockMvc.perform(post("/admin/users/{id}/delete", adminUser.getId())
            .with(csrf())
            .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
        .andExpect(status().is3xxRedirection());

    Thread.sleep(500);

    List<AuditLog> logs = auditLogRepository.findByUserIdAndAction(adminUser.getId(), ActionType.USER_DELETE);
    assertThat(logs).hasSize(1);
    AuditLog log = logs.get(0);
    assertThat(log.getResult()).isEqualTo(ResultType.FAILURE);
    assertThat(log.getDetails()).contains("cannot delete your own account");
  }

  @Test
  void shouldLogSharedKeyAddWhenAdminAddsKey() throws Exception {
    String keyJson = objectMapper.writeValueAsString(
        Map.of("provider", "OPENAI", "keyValue", "sk-test123")
    );

    mockMvc.perform(post("/api/admin/shared-keys")
            .contentType(MediaType.APPLICATION_JSON)
            .content(keyJson)
            .with(csrf())
            .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
        .andExpect(status().isOk());

    Thread.sleep(500);

    List<AuditLog> logs = auditLogRepository.findByUserIdAndAction(adminUser.getId(), ActionType.SHARED_KEY_GENERATE);
    assertThat(logs).hasSize(1);
    AuditLog log = logs.get(0);
    assertThat(log.getResult()).isEqualTo(ResultType.SUCCESS);
    assertThat(log.getTarget()).contains("OPENAI");
  }

  @Test
  void shouldLogSharedKeyDeleteWhenAdminDeletesKey() throws Exception {
    String keyJson = objectMapper.writeValueAsString(
        Map.of("provider", "ANTHROPIC", "keyValue", "sk-anthropic")
    );
    String response = mockMvc.perform(post("/api/admin/shared-keys")
            .contentType(MediaType.APPLICATION_JSON)
            .content(keyJson)
            .with(csrf())
            .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    Long keyId = objectMapper.readTree(response).get("id").asLong();

    mockMvc.perform(delete("/api/admin/shared-keys/{id}", keyId)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}")
            .with(csrf())
            .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
        .andExpect(status().isNoContent());

    Thread.sleep(500);

    List<AuditLog> logs = auditLogRepository.findByUserIdAndAction(adminUser.getId(), ActionType.SHARED_KEY_DELETE);
    assertThat(logs).hasSize(1);
    AuditLog log = logs.get(0);
    assertThat(log.getResult()).isEqualTo(ResultType.SUCCESS);
    assertThat(log.getTarget()).contains(keyId.toString());
  }
}