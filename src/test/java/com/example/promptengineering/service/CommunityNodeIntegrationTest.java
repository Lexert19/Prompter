package com.example.promptengineering.service;

import com.example.promptengineering.component.NodeTunnelRegistry;
import com.example.promptengineering.entity.HostedNode;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.exception.UserAlreadyExistsException;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.model.Message;
import com.example.promptengineering.model.RequestBuilder;
import com.example.promptengineering.model.TextContent;
import com.example.promptengineering.repository.HostedNodeRepository;
import com.example.promptengineering.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class CommunityNodeIntegrationTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private HostedNodeRepository nodeRepo;

    @Autowired
    private UserRepository userRepo;

    @MockBean
    private NodeTunnelRegistry nodeTunnelRegistry;

    @Autowired
    private UserService userService;

    private User owner;
    private User requester;
    private HostedNode publicNode;

    @BeforeEach
    void setup() throws UserAlreadyExistsException {
        owner = userService.createUser("owner-" + UUID.randomUUID() + "@test.com",
                "test123", List.of(AppRole.USER));

        requester = userService.createUser("req-" + UUID.randomUUID() + "@test.com",
                "test123", List.of(AppRole.USER));

        publicNode = new HostedNode();
        publicNode.setOwner(owner);
        publicNode.setNodeName("public-llama");
        publicNode.setModelName("llama3-8b");
        publicNode.setModelFamily("community");
        publicNode.setAuthToken(UUID.randomUUID().toString().replace("-", ""));
        publicNode.setStatus(HostedNode.Status.ONLINE);
        publicNode.setAllowPublicUse(true);
        publicNode = nodeRepo.save(publicNode);

        Mockito.when(nodeTunnelRegistry.isOnline(publicNode.getId())).thenReturn(true);
        Mockito.when(nodeTunnelRegistry.sendRequest(eq(publicNode.getId()), anyMap()))
                .thenAnswer(inv -> CompletableFuture
                        .completedFuture("{\"model\":\"" + publicNode.getModelName()
                                + "\",\"reply\":\"Hello from tunnel\"}"));
    }

    @Test
    void publicNode_shouldServeRequestViaTunnel() {
        RequestBuilder builder = new RequestBuilder().communityNode(publicNode.getId())
                .addMessage(txt("user", "test prompt")).temperature(0.7);

        Flux<ServerSentEvent<String>> result = chatService.makeRequest(builder,
                requester);

        StepVerifier.create(result).assertNext(event -> {
            assertThat(event.data()).contains("Hello from tunnel");
            assertThat(event.data()).contains("llama3-8b");
        }).verifyComplete();

        Mockito.verify(nodeTunnelRegistry).sendRequest(eq(publicNode.getId()),
                argThat(map -> "llama3-8b".equals(map.get("model"))
                        && map.containsKey("messages")
                        && !map.containsKey("correlationId")));
    }

    @Test
    void privateNode_shouldRejectOtherUser() {
        publicNode.setAllowPublicUse(false);
        nodeRepo.save(publicNode);

        RequestBuilder builder = new RequestBuilder().communityNode(publicNode.getId())
                .addMessage(txt("user", "x"));

        Flux<ServerSentEvent<String>> result = chatService.makeRequest(builder,
                requester);

        StepVerifier.create(result).assertNext(event -> {
            assertThat(event.event()).isEqualTo("error");
            assertThat(event.data()).contains("Brak dostępu");
        }).verifyComplete();
    }

    @Test
    void offlineNode_shouldReturnError() {
        Mockito.when(nodeTunnelRegistry.isOnline(publicNode.getId())).thenReturn(false);

        RequestBuilder builder = new RequestBuilder().communityNode(publicNode.getId())
                .addMessage(txt("user", "x"));

        Flux<ServerSentEvent<String>> result = chatService.makeRequest(builder,
                requester);

        StepVerifier.create(result)
                .assertNext(event -> assertThat(event.data()).contains("isn't active"))
                .verifyComplete();
    }

    private Message txt(String role, String text) {
        return new Message(role, List.of(new TextContent(text)));
    }
}
