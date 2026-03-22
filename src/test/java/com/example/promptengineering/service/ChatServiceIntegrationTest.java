package com.example.promptengineering.service;

import com.example.promptengineering.entity.Model;
import com.example.promptengineering.entity.SharedKey;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.Content;
import com.example.promptengineering.model.Message;
import com.example.promptengineering.model.RequestBuilder;
import com.example.promptengineering.model.TextContent;
import com.example.promptengineering.repository.ModelRepository;
import com.example.promptengineering.repository.SharedKeyRepository;
import com.example.promptengineering.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ChatServiceIntegrationTest {

    @Autowired
    private ChatService chatService;
    @Autowired
    private SharedKeyRepository sharedKeyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelRepository modelRepository;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private User adminUser;
    private Model geminiModel;
    private SharedKey geminiSharedKey;
    private User owner;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new IllegalStateException("Admin user not found"));

        geminiModel = modelRepository.findByProviderAndGlobalTrue("GEMINI").stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No global Gemini model found"));

        SharedKey tempKey = sharedKeyRepository.findByProvider("GEMINI").get(0);
        geminiSharedKey = sharedKeyRepository.findByIdWithOwner(tempKey.getId()).orElseThrow();
        owner = geminiSharedKey.getOwner();
    }

    @Test
    void shouldCallGeminiApiAndIncreasePoints() throws Exception {
        double initialPoints = owner.getPoints();
        int initialUsage = geminiSharedKey.getUsageCount();

        RequestBuilder request = new RequestBuilder();
        request.setProvider("GEMINI");
        request.setModel(geminiModel.getName());
        request.setUrl(geminiModel.getUrl());
        request.setUseSharedKeys(true);
        request.setStream(true);
        request.setMaxTokens(100);
        request.setTemperature(0.0);

        TextContent content = new TextContent();
        content.setType("text");
        content.setText("Cześć, jak się nazywasz?");
        Message message = new Message("user", List.of(content));
        request.setMessages(List.of(message));

        Flux<ServerSentEvent<String>> result = chatService.makeRequest(request, adminUser);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> lastData = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        result.subscribe(
                event -> {
                    if (event.data() != null && !event.data().equals("[DONE]")) {
                        lastData.set(event.data());
                    }
                    if ("[DONE]".equals(event.data())) {
                        latch.countDown();
                    }
                },
                err -> {
                    error.set(err);
                    latch.countDown();
                },
                latch::countDown
        );

        boolean completed = latch.await(60, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(error.get()).isNull();
        //assertThat(lastData.get()).isNotNull().contains("Gemini");

        Thread.sleep(5000);
        User refreshedOwner = userRepository.findById(owner.getId()).orElseThrow();
        SharedKey refreshedKey = sharedKeyRepository.findById(geminiSharedKey.getId()).orElseThrow();

        assertThat(refreshedOwner.getPoints()).isGreaterThan(initialPoints);
        assertThat(refreshedKey.getUsageCount()).isEqualTo(initialUsage + 1);
    }

    @Test
    void shouldNotIncreasePointsOnError() throws Exception {
        User owner = geminiSharedKey.getOwner();
        double initialPoints = owner.getPoints();
        int initialUsage = geminiSharedKey.getUsageCount();

        RequestBuilder request = new RequestBuilder();
        request.setProvider("GEMINI");
        request.setModel("non-existent-model");
        request.setUrl(geminiModel.getUrl());
        request.setUseSharedKeys(true);
        request.setStream(true);
        request.setMaxTokens(100);
        request.setTemperature(0.0);

        TextContent content = new TextContent();
        content.setType("text");
        content.setText("Hello");
        Message message = new Message("user", List.of(content));
        request.setMessages(List.of(message));

        Flux<ServerSentEvent<String>> result = chatService.makeRequest(request, adminUser);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> errorEvent = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        result.subscribe(
                event -> {
                    if ("error".equals(event.event())) {
                        errorEvent.set(event.data());
                        latch.countDown();
                    }
                },
                err -> {
                    error.set(err);
                    latch.countDown();
                },
                latch::countDown
        );

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();
        assertThat(error.get()).isNull();
        assertThat(errorEvent.get()).isNotNull().contains("error");

        User refreshedOwner = userRepository.findById(owner.getId()).orElseThrow();
        SharedKey refreshedKey = sharedKeyRepository.findById(geminiSharedKey.getId()).orElseThrow();

        assertThat(refreshedOwner.getPoints()).isEqualTo(initialPoints);
        //assertThat(refreshedKey.getUsageCount()).isEqualTo(initialUsage);
    }
}