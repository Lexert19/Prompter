package com.example.promptengineering.e2e;

import com.example.promptengineering.e2e.pages.ChatPage;
import com.example.promptengineering.e2e.pages.LoginPage;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
public class ChatE2ETest extends BaseE2ETest {
    private static WireMockServer wireMockServer;
    private LoginPage loginPage;
    private ChatPage chatPage;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void initPages() {
        loginPage = new LoginPage(driver);
        chatPage = new ChatPage(driver);
    }

    @Test
    void sendMessageWithGeminiModel() {
        stubFor(post(urlEqualTo("/client/chat"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.TEXT_EVENT_STREAM_VALUE)
                        .withBody("data: {\"choices\":[{\"delta\":{\"content\":\"Odpowiedź z mocka\"}}]}\n\ndata: [DONE]\n\n")));

        driver.get(baseUrl + "/auth/login");
        loginPage.login("admin@example.com", "admin123");
        driver.get(baseUrl + "/chat");

        chatPage.openSettings();
        chatPage.selectModel("Gemini 3 Flash");
        chatPage.enableSharedKeys();

        chatPage.sendMessage("Witaj, AI!");

        String lastMessage = chatPage.getLastMessageText();
        assertTrue(lastMessage.contains("Odpowiedź z mocka"));
    }
}