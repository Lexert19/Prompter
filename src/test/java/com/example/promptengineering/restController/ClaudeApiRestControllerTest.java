package com.example.promptengineering.restController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOAuth2Login;

import java.util.Collections;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "360000")
public class ClaudeApiRestControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithMockUser   
    public void testMakeRequest() {
        OAuth2User mockUser = new DefaultOAuth2User(
                Collections.singletonList(new SimpleGrantedAuthority("SCOPE_message:read")),
                Collections.singletonMap("email", "dominikch19@gmail.com"), 
                "email");

        String jsonBody = """

                {
                    "model": "claude-3-haiku-20240307",
                    "max_tokens": 1024,
                    "messages": [
                        {"role": "user", "content": "Hello, world"}
                    ]
                }
                                """;

        webTestClient
                .mutateWith(mockOAuth2Login()
                        .oauth2User(mockUser))
                .post()
                .uri("/claude/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(jsonBody))
                .exchange()
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    System.out.println(response);
                    assert responseBody != null;
                    assert response.getStatus().is2xxSuccessful();
                    //assert response.getResponseHeaders().getContentType().equals(MediaType.APPLICATION_JSON);
                });

    }
}
