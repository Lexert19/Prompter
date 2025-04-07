package com.example.promptengineering.restController;
import com.example.promptengineering.model.ProjectResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureWebTestClient
public class ProjectControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "dominikch19@gmail.com")
    public void shouldCreateProject() throws JsonProcessingException {
        String projectName = "Nowy projekt";
        String requestBody = objectMapper.writeValueAsString(projectName);

        WebTestClient.ResponseSpec responseSpec = webTestClient.post()
                .uri("/api/projects/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .exchange();

        responseSpec.expectStatus().isOk()
                .expectBody(ProjectResponse.class)
                .consumeWith(response -> {
                    ProjectResponse projectResponse = response.getResponseBody();
                    assertThat(projectResponse).isNotNull();
                    assertThat(projectResponse.getName()).isEqualTo(projectName);
                    assertThat(projectResponse.getId()).isNotNull();
                    assertThat(projectResponse.getFiles()).isEmpty();
                });
    }
}