package com.example.promptengineering.restController;

import com.example.promptengineering.dto.ModelDto;
import com.example.promptengineering.entity.Model;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.ModelRepository;
import com.example.promptengineering.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ModelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Model userModel;
    private Model globalModel;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode("password"));
        testUser = userRepository.save(testUser);

        userModel = new Model();
        userModel.setName("My Model");
        userModel.setProvider("OPENAI");
        userModel.setUser(testUser);
        userModel.setGlobal(false);
        userModel = modelRepository.save(userModel);

        globalModel = new Model();
        globalModel.setName("Global Model");
        globalModel.setProvider("ANTHROPIC");
        globalModel.setUser(null);
        globalModel.setGlobal(true);
        globalModel = modelRepository.save(globalModel);
    }

    @Test
    void shouldGetUserModels() throws Exception {
        mockMvc.perform(get("/api/models/user-models").with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("My Model"))
                .andExpect(jsonPath("$[0].provider").value("OPENAI"));
    }

    @Test
    void shouldGetGlobalModels() throws Exception {
        mockMvc.perform(get("/api/models/global-models").with(user(testUser)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetAllModels() throws Exception {
        mockMvc.perform(get("/api/models/all-models").with(user(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItems("My Model", "Global Model")));
    }

    @Test
    void shouldAddUserModel() throws Exception {
        ModelDto newModel = new ModelDto();
        newModel.setName("New Model");
        newModel.setProvider("OPENAI");
        newModel.setType("text");

        mockMvc.perform(post("/api/models/user-models").with(user(testUser)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newModel)))
                .andExpect(status().isOk())
                .andExpect(content().string("Model saved successfully"));

        assertThat(modelRepository.findByUser(testUser)).hasSize(2);
    }

    @Test
    void shouldEditUserModel() throws Exception {
        ModelDto editDto = new ModelDto();
        editDto.setName("Updated Name");
        editDto.setProvider("ANTHROPIC");

        mockMvc.perform(put("/api/models/user-models/{id}", userModel.getId())
                .with(csrf()).with(user(testUser)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Model updated successfully"));

        Model updated = modelRepository.findById(userModel.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void shouldNotEditModelOfAnotherUser() throws Exception {
        User otherUser = new User();
        otherUser.setEmail("other@example.com");
        otherUser.setPassword(passwordEncoder.encode("pass"));
        otherUser = userRepository.save(otherUser);

        Model otherModel = new Model();
        otherModel.setName("Other's Model");
        otherModel.setProvider("OPENAI");
        otherModel.setUser(otherUser);
        otherModel = modelRepository.save(otherModel);

        ModelDto editDto = new ModelDto();
        editDto.setName("Hacked");

        mockMvc.perform(put("/api/models/user-models/{id}", otherModel.getId())
                .with(csrf()).with(user(testUser)).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editDto)))
                .andExpect(status().isNotFound());

        Model unchanged = modelRepository.findById(otherModel.getId()).orElseThrow();
        assertThat(unchanged.getName()).isEqualTo("Other's Model");
    }

    @Test
    void shouldDeleteUserModel() throws Exception {
        mockMvc.perform(delete("/api/models/user-models/{id}", userModel.getId())
                .with(csrf()).with(user(testUser))).andExpect(status().isOk())
                .andExpect(content().string("Model deleted successfully"));

        assertThat(modelRepository.findById(userModel.getId())).isEmpty();
    }

    @Test
    void shouldNotDeleteModelOfAnotherUser() throws Exception {
        User otherUser = new User();
        otherUser.setEmail("other2@example.com");
        otherUser.setPassword(passwordEncoder.encode("pass"));
        otherUser = userRepository.save(otherUser);

        Model otherModel = new Model();
        otherModel.setName("Other's Model");
        otherModel.setProvider("OPENAI");
        otherModel.setUser(otherUser);
        otherModel = modelRepository.save(otherModel);

        mockMvc.perform(delete("/api/models/user-models/{id}", otherModel.getId())
                .with(csrf()).with(user(testUser))).andExpect(status().isNotFound());

        assertThat(modelRepository.findById(otherModel.getId())).isPresent();
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/models/user-models"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/models/user-models")
                .contentType(MediaType.APPLICATION_JSON).with(csrf())
                .content(objectMapper.writeValueAsString(new ModelDto())))
                .andExpect(status().isUnauthorized());
    }
}
