package com.example.promptengineering.restController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import com.example.promptengineering.entity.ResetToken;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.ResetTokenRepository;
import com.example.promptengineering.repository.UserRepository;

import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureWebTestClient
public class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ResetTokenRepository resetTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        this.webTestClient = webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(10000))
                .build();
        String login = "testuser123@wp.pl";
        String password = "testpassword123";

        userRepository.findByEmail(login)
                .flatMap(existingUser -> {
                    existingUser.setPassword(passwordEncoder.encode(password));
                    return userRepository.save(existingUser);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    User newUser = new User();
                    newUser.setEmail(login);
                    newUser.setPassword(passwordEncoder.encode(password));
                    return userRepository.save(newUser);
                }))
                .block();
    }

    @Test
    public void testLoginSuccess() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", "testuser123@wp.pl");
        formData.add("password", "testpassword123");

        FluxExchangeResult<String> result = webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", "/")
                .expectCookie().exists("SESSION")
                .returnResult(String.class);

        // List<ResponseCookie> cookies = result.getResponseCookies().get("SESSION");
        // assertNotNull(cookies, "Session cookie should be present");
        // assertFalse(cookies.isEmpty(), "Session cookie list should not be empty");
    }

    @Test
    public void testShowLoginForm() {
        webTestClient.get().uri("/auth/login")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String responseBody = new String(response.getResponseBody());
                    assertTrue(responseBody.contains("Login"));
                });
    }

    @Test
    public void testLoginFailure() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", "testuser123@wp.pl");
        formData.add("password", "wrongpassword");


        FluxExchangeResult<String> result = webTestClient.post().uri("/auth/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().isFound()
                .expectHeader().valueEquals("Location", "/auth/login?error=true")
                .returnResult(String.class);

        HttpStatusCode status = result.getStatus();
        System.out.println("Response body: " + result.getResponseBody().blockFirst());

    }

    @Test
    public void testPasswordResetRequest() {
        resetTokenRepository.deleteByUserLogin("testuser123@wp.pl").block();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("email", "testuser123@wp.pl");

        FluxExchangeResult<String> result = webTestClient
                .mutate()
                .responseTimeout(Duration.ofSeconds(10000))
                .build()
                .post().uri("/auth/reset-password-request")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .returnResult(String.class);
        System.out.println("Response body: " + result.getResponseBody().blockFirst(Duration.ofSeconds(1000)));
        HttpStatusCode status = result.getStatus();

        assertTrue(status.is3xxRedirection());
        ResetToken resetToken = resetTokenRepository.findByUserLogin("testuser123@wp.pl")
                .switchIfEmpty(Mono
                        .error(() -> new AssertionError("Reset token not found for the user")))
                .blockFirst();
    }

    @Test
    public void testPasswordResetConfirmationSuccess() {
        String uniqueToken = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken();
        resetToken.setToken(uniqueToken);
        resetToken.setUserLogin("testuser123@wp.pl");
        resetToken.setCreationTime(LocalDateTime.now());
        resetTokenRepository.save(resetToken).block();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", resetToken.getToken());
        formData.add("password", "nowe_haslo");
        formData.add("password_confirmation", "nowe_haslo");

        webTestClient.post().uri("/auth/reset-password-confirm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange();

        User user = userRepository.findByEmail(resetToken.getUserLogin())
                .blockOptional().orElseThrow(() -> new AssertionError("User not found"));

        assertTrue(passwordEncoder.matches("nowe_haslo", user.getPassword()));

        user.setPassword(passwordEncoder.encode("testpassword123"));
        userRepository.save(user);

    }

    @Test
    public void testPasswordResetConfirmationFailure() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", "invalid_token");
        formData.add("password", "nowe_haslo");
        formData.add("password_confirmation", "nowe_haslo");

        webTestClient.post().uri("/auth/reset-password-confirm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange();
    }

    @Test
    public void testExpiredResetToken() {
        String id = UUID.randomUUID().toString();
        ResetToken resetToken = new ResetToken();
        resetToken.setToken(id);
        resetToken.setUserLogin("testuser123@wp.pl");
        resetToken.setCreationTime(LocalDateTime.now().minusHours(2));
        resetTokenRepository.save(resetToken).block();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", id);
        formData.add("password", "nowe_haslo");
        formData.add("password_confirmation", "nowe_haslo");

        webTestClient.post().uri("/auth/reset-password-confirm")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .exchange()
                .expectStatus().isOk();
    }

}
