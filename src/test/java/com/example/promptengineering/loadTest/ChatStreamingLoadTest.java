package com.example.promptengineering.loadTest;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.example.promptengineering.component.JwtTokenProvider;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("loadtest")
@Tag("loadtest")
public class ChatStreamingLoadTest {

    @LocalServerPort
    int port;

    @Autowired
    JwtTokenProvider jwt;

    @Autowired
    UserService userService;

    @Autowired
    ObjectMapper objectMapper;

    private String testToken;

    private WebClient createWebClient(int maxConnections) {
        ConnectionProvider provider = ConnectionProvider.builder("custom")
                .maxConnections(maxConnections).pendingAcquireMaxCount(-1)
                .pendingAcquireTimeout(Duration.ofSeconds(30)).build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(30))
                        .addHandlerLast(new WriteTimeoutHandler(30)))
                .responseTimeout(Duration.ofSeconds(30));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + testToken).build();
    }

    @BeforeEach
    void setUpToken() throws Exception {
        User testUser;
        try {
            testUser = userService.findUserByEmail("loadtest@test.com");
        } catch (Exception e) {
            testUser = userService.createUser("loadtest@test.com", "Test123!@#",
                    List.of(AppRole.USER));
        }

        testToken = jwt.generateAccessToken(testUser);
        System.out.println("Generated JWT: " + testToken);
    }

    @Test
    public void howManyConcurrentStreams() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
                Map.of("url", "http://localhost:" + port + "/mock-ai/chat/completions",
                        "provider", "OPENAI", "key", "test-key", "model", "mock-model",
                        "messages", List.of(Map.of("role", "user", "content",
                                List.of(Map.of("type", "text", "text", "test"))))));

        int[] probes = {400, 10000};

        for (int concurrent : probes) {
            System.out.println("\n=== TEST " + concurrent + " STREAMS ===");

            WebClient client = createWebClient(concurrent);

            CountDownLatch latch = new CountDownLatch(concurrent);
            AtomicInteger ok = new AtomicInteger();
            AtomicInteger failed = new AtomicInteger();

            long start = System.currentTimeMillis();

            for (int i = 0; i < concurrent; i++) {
                client.post().uri("/client/chat").contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM).bodyValue(requestBody)
                        .retrieve().bodyToFlux(String.class)
                        .doOnError(e -> failed.incrementAndGet())
                        .doOnComplete(ok::incrementAndGet).subscribe(data -> {
                        }, error -> {
                        }, () -> latch.countDown());
            }

            boolean finished = latch.await(90, TimeUnit.SECONDS);
            long duration = System.currentTimeMillis() - start;

            System.out.printf("Finished: %s, OK: %d, FAILED: %d, Time: %d ms%n", finished,
                    ok.get(), failed.get(), duration);

            if (!finished || failed.get() > concurrent * 0.05) {
                System.out.println("Test aborted – too many errors or timeout.");
                break;
            }

            Thread.sleep(5000);
        }
    }
}
