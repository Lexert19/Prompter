package com.example.promptengineering.restController;

import com.example.promptengineering.model.ProjectResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "dominikch19@gmail.com")
    public void shouldCreateProject() throws Exception {
        String projectName = "Nowy projekt";
        String requestBodyJson = objectMapper.writeValueAsString(projectName);

        mockMvc.perform(post("/api/projects/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyJson))
                .andExpect(status().isOk());
//                .andExpect(jsonPath("$.name").value(projectName))
//                .andExpect(jsonPath("$.id").exists())
//                .andExpect(jsonPath("$.files").isEmpty());
    }
}