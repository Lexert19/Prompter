package com.example.promptengineering.component;

import com.example.promptengineering.entity.HostedNode;
import com.example.promptengineering.repository.HostedNodeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NodeTunnelWebSocketHandler implements WebSocketHandler {

    private final NodeTunnelRegistry registry;
    private final HostedNodeRepository nodeRepo;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String path = session.getHandshakeInfo().getUri().getPath();
        UUID nodeId = UUID.fromString(path.substring(path.lastIndexOf('/') + 1));

        String auth = session.getHandshakeInfo().getHeaders().getFirst("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return session.close();
        }
        String token = auth.substring(7);

        return Mono.fromCallable(() -> nodeRepo.findByAuthToken(token).orElseThrow())
                .flatMap(node -> {
                    if (!node.getId().equals(nodeId))
                        return session.close();
                    registry.register(nodeId, session);
                    node.setStatus(HostedNode.Status.ONLINE);
                    nodeRepo.save(node);

                    return session.receive().map(msg -> msg.getPayloadAsText())
                            .doOnNext(text -> {
                                try {
                                    JsonNode json = objectMapper.readTree(text);
                                    String corrId = json.get("correlationId").asText();
                                    String result = json.get("result").asText();
                                    registry.handleResponse(corrId, result);
                                } catch (Exception ignored) {
                                }
                            }).then();
                }).doFinally(sig -> {
                    registry.unregister(nodeId);
                    nodeRepo.findById(nodeId).ifPresent(n -> {
                        n.setStatus(HostedNode.Status.OFFLINE);
                        nodeRepo.save(n);
                    });
                });
    }
}
