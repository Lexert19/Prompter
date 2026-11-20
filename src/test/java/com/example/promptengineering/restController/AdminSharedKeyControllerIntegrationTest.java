package com.example.promptengineering.restController;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.repository.SharedKeyRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminSharedKeyControllerIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/admin/shared-keys";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SharedKeyRepository sharedKeyRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User normalUser;
    private String adminEmail;
    private String userEmail;
    private RequestPostProcessor asAdmin;
    private RequestPostProcessor asRegularUser;

    @BeforeEach
    void setUp() {
        adminEmail = "admin-" + UUID.randomUUID() + "@test.com";
        userEmail  = "user-"  + UUID.randomUUID() + "@test.com";

        adminUser  = userService.createUser(adminEmail, "adminPass", List.of(AppRole.ADMIN));
        normalUser = userService.createUser(userEmail,  "userPass",  List.of(AppRole.USER));

        asAdmin       = user(userService.loadUserByUsername(adminEmail));
        asRegularUser = user(userService.loadUserByUsername(userEmail));
    }

    @AfterEach
    void tearDown() {
        sharedKeyRepository.deleteAll();
        if (adminUser  != null) userRepository.delete(adminUser);
        if (normalUser != null) userRepository.delete(normalUser);
    }

    @Test
    void anonymous_cannotList() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymous_cannotAdd_andDbStaysEmpty() throws Exception {
        String json = objectMapper.writeValueAsString(
                Map.of("provider", "OPENAI", "keyValue", "sk-attacker"));
        long before = sharedKeyRepository.count();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        assertThat(sharedKeyRepository.count())
                .as("anonymous user must not be able to add a shared key")
                .isEqualTo(before);
    }

    @Test
    void anonymous_cannotDelete() throws Exception {
        createSharedKey("OPENAI", "sk-existing");
        Long id = sharedKeyRepository.findAll().getFirst().getId();

        mockMvc.perform(delete(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        assertThat(sharedKeyRepository.findById(id))
                .as("anonymous user must not be able to delete a shared key")
                .isPresent();
    }

    @Test
    void regularUser_cannotList() throws Exception {
        mockMvc.perform(get(BASE_URL).with(asRegularUser))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUser_cannotAdd_andDbStaysEmpty() throws Exception {
        String json = objectMapper.writeValueAsString(
                Map.of("provider", "OPENAI", "keyValue", "sk-user-tried"));
        long before = sharedKeyRepository.count();

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf())
                        .with(asRegularUser))
                .andExpect(status().isForbidden());

        assertThat(sharedKeyRepository.count())
                .as("regular user must not be able to add a shared key")
                .isEqualTo(before);
    }

    @Test
    void regularUser_cannotDelete() throws Exception {
        createSharedKey("OPENAI", "sk-existing");
        Long id = sharedKeyRepository.findAll().getFirst().getId();

        mockMvc.perform(delete(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf())
                        .with(asRegularUser))
                .andExpect(status().isForbidden());

        assertThat(sharedKeyRepository.findById(id))
                .as("regular user must not be able to delete a shared key")
                .isPresent();
    }

    @Test
    void admin_canList() throws Exception {
        mockMvc.perform(get(BASE_URL).with(asAdmin))
                .andExpect(status().isOk());
    }

    @Test
    void admin_canAdd_andResponseHasOnlyIdAndMessage() throws Exception {
        String json = objectMapper.writeValueAsString(
                Map.of("provider", "OPENAI", "keyValue", "sk-admin-created"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf())
                        .with(asAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.message").value("Added"))
                .andExpect(jsonPath("$.keyValue").doesNotExist())
                .andExpect(jsonPath("$.encrypted").doesNotExist());
    }

    @Test
    void admin_canDelete_andKeyRemovedFromDb() throws Exception {
        createSharedKey("OPENAI", "sk-to-delete");
        Long id = sharedKeyRepository.findAll().getFirst().getId();

        mockMvc.perform(delete(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf())
                        .with(asAdmin))
                .andExpect(status().isNoContent());

        assertThat(sharedKeyRepository.findById(id)).isEmpty();
    }

    @Test
    void admin_deleteNonExistent_returns404() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", 9_999_999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf())
                        .with(asAdmin))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_doesNotLeakKeyValueOrEncryptedValue() throws Exception {
        String secret = "sk-SECRET-" + UUID.randomUUID();
        createSharedKey("OPENAI", secret);

        String response = mockMvc.perform(get(BASE_URL).with(asAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response)
                .as("shared key list must not contain the key value")
                .doesNotContain(secret);
        assertThat(response)
                .as("list must not contain keyValue field")
                .doesNotContain("keyValue");
        assertThat(response)
                .as("list must not contain encrypted field")
                .doesNotContain("encrypted");
        assertThat(response).contains("provider", "working", "usageCount");
    }

    @Test
    void addSharedKey_doesNotEchoSecretInResponse() throws Exception {
        String secret = "sk-SECRET-" + UUID.randomUUID();
        String json = objectMapper.writeValueAsString(
                Map.of("provider", "OPENAI", "keyValue", secret));

        String response = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf())
                        .with(asAdmin))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(response).doesNotContain(secret);
    }

    private void createSharedKey(String provider, String keyValue) throws Exception {
        String json = objectMapper.writeValueAsString(
                Map.of("provider", provider, "keyValue", keyValue));
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(csrf())
                        .with(asAdmin))
                .andExpect(status().isOk());
    }
}

