package com.example.promptengineering.service;

import com.example.promptengineering.component.NodeTunnelRegistry;
import com.example.promptengineering.entity.HostedNode;
import com.example.promptengineering.repository.HostedNodeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.example.promptengineering.entity.SharedKey;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.entity.UserFile;
import com.example.promptengineering.model.Content;
import com.example.promptengineering.model.ImageContent;
import com.example.promptengineering.model.Message;
import com.example.promptengineering.repository.SharedKeyRepository;
import com.example.promptengineering.repository.UserRepository;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import com.example.promptengineering.model.RequestBuilder;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
public class ChatService {
    private final ObjectMapper objectMapper;
    private final WebClient webClient;
    private final FileStorageService fileStorageService;
    private final SharedKeyService sharedKeyService;
    private final EncryptionService encryptionService;
    private final SharedKeyRepository sharedKeyRepository;
    private final UserRepository userRepository;
    private final TokenTrackingService tokenTrackingService;
    private final HostedNodeRepository hostedNodeRepository;
    private final NodeTunnelRegistry nodeTunnelRegistry;

    public ChatService(ObjectMapper objectMapper, WebClient.Builder webClientBuilder,
            FileStorageService fileStorageService, SharedKeyService sharedKeyService,
            EncryptionService encryptionService, SharedKeyRepository sharedKeyRepository,
            UserRepository userRepository, TokenTrackingService tokenTrackingService,
            HostedNodeRepository hostedNodeRepository,
            NodeTunnelRegistry nodeTunnelRegistry) {
        this.objectMapper = objectMapper;
        this.webClient = webClientBuilder.codecs(
                configure -> configure.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
        this.fileStorageService = fileStorageService;
        this.sharedKeyService = sharedKeyService;
        this.encryptionService = encryptionService;
        this.sharedKeyRepository = sharedKeyRepository;
        this.userRepository = userRepository;
        this.tokenTrackingService = tokenTrackingService;
        this.hostedNodeRepository = hostedNodeRepository;
        this.nodeTunnelRegistry = nodeTunnelRegistry;
    }

    private Mono<Void> attachBase64Images(RequestBuilder request, User user) {
        return Mono.fromRunnable(() -> {
            for (Message msg : request.getMessages()) {
                for (Content content : msg.getContent()) {
                    if (isImageWithFileId(content)) {
                        enrichContentWithBase64((ImageContent) content, user);
                    }
                }
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private void enrichContentWithBase64(ImageContent content, User user) {
        try {
            UserFile userFile = fileStorageService.getUserFile(content.getFileId(), user);
            Path base64Path = Paths.get(userFile.getBase64Path());
            String base64Data = Files.readString(base64Path, StandardCharsets.UTF_8);
            content.setData(base64Data);
            content.setMediaType(userFile.getContentType());
            content.setFileId(null);
        } catch (Exception e) {
            log.error("Failed to attach image {}", e.getMessage());
        }
    }

    private Mono<String> buildRequestBodyJson(RequestBuilder request) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(request.build()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Flux<ServerSentEvent<String>> executeRequest(RequestBuilder request,
                                                         String json) {
        WebClient.RequestBodySpec requestSpec = buildHttpRequest(request, json);

        return requestSpec.retrieve().bodyToFlux(String.class)
                .timeout(Duration.ofMinutes(5)).filter(line -> !line.isBlank())
                .doOnComplete(() -> {
                    int completion = tokenTrackingService.getCompletionTokens();
                    addPointsForSharedKey(request.getSharedKeyId(), completion);
                }).map(this::toServerSentEvent)
                .doOnCancel(() -> log.debug("SSE stream cancelled by client"))
                .onErrorResume(this::handleError);
    }

    private Flux<ServerSentEvent<String>> executeCommunityRequest(RequestBuilder request,
                                                                  String json) {
        UUID nodeId = request.getCommunityNodeId();

        HostedNode node = hostedNodeRepository
                .findByIdAndStatus(nodeId, HostedNode.Status.ONLINE)
                .orElseThrow(() -> new IllegalStateException("Node offline"));

        if (!nodeTunnelRegistry.isOnline(nodeId)) {
            throw new IllegalStateException("Node isn't active");
        }

        try {
            Map<String, Object> payload = objectMapper.readValue(json, Map.class);
            payload.put("model", node.getModelName());

            return Mono.fromFuture(nodeTunnelRegistry.sendRequest(nodeId, payload))
                    .timeout(Duration.ofMinutes(2)).flatMapMany(result -> {
                        return Flux.just(toServerSentEvent(result));
                    }).doFinally(s -> {
                        int completion = tokenTrackingService.getCompletionTokens();
                    });

        } catch (Exception e) {
            return handleError(e);
        }
    }

    private WebClient.RequestBodySpec buildHttpRequest(RequestBuilder request,
                                                       String json) {
        String finalUrl = request.getUrl();

        WebClient.RequestBodySpec spec = (WebClient.RequestBodySpec) webClient.post()
                .uri(finalUrl).contentType(MediaType.APPLICATION_JSON).bodyValue(json);

        if ("ANTHROPIC".equalsIgnoreCase(request.getProvider())) {
            spec.header("x-api-key", request.getKey())
                    .header("anthropic-version", "2023-06-01")
                    .header("anthropic-beta", "prompt-caching-2024-07-31");
        } else if ("GEMINI".equalsIgnoreCase(request.getProvider())) {
            spec.header("x-goog-api-key", request.getKey());
        } else {
            spec.header("Authorization", "Bearer " + request.getKey());
        }

        return spec;
    }

    private ServerSentEvent<String> toServerSentEvent(String line) {
        return ServerSentEvent.<String>builder().data(line).build();
    }

    private Flux<ServerSentEvent<String>> handleError(Throwable e) {
        String errorDetails;

        if (e instanceof WebClientResponseException webClientEx) {
            String responseBody = webClientEx.getResponseBodyAsString();
            errorDetails = "API Error: " + responseBody;
        } else {
            errorDetails = e.getMessage();
        }

        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", errorDetails);
        String errorJson;
        try {
            errorJson = objectMapper.writeValueAsString(errorMap);
        } catch (Exception ex) {
            errorJson = "{\"error\":\"serialization failed\"}";
        }
        log.debug("API request error: {}", errorJson);

        return Flux.just(
                ServerSentEvent.<String>builder().event("error").data(errorJson).build());
    }

    private boolean isImageWithFileId(Content content) {
        return "image".equals(content.getType()) && content.getFileId() != null;
    }

    public Flux<ServerSentEvent<String>> makeRequest(RequestBuilder request, User user) {
        Mono<RequestBuilder> preparedRequest = Mono.fromCallable(() -> {
            if (request.getCommunityNodeId() != null) {
                return applyCommunityNode(request, user);
            }
            if (request.isUseSharedKeys()) {
                return prepareWithSharedKey(request);
            }
            return request;
        }).subscribeOn(Schedulers.boundedElastic());

        return preparedRequest.flatMapMany(req -> attachBase64Images(req, user)
                .then(buildRequestBodyJson(req)).flatMapMany(json -> {
                    if (req.getCommunityNodeId() != null) {
                        return executeCommunityRequest(req, json);
                    } else {
                        return executeRequest(req, json);
                    }
                })).doOnError(e -> blockSharedKeyIfUsed(request))
                .onErrorResume(this::handleError);
    }

    private RequestBuilder applyCommunityNode(RequestBuilder request, User user) {
        UUID nodeId = request.getCommunityNodeId();
        HostedNode node = hostedNodeRepository.findById(nodeId)
                .orElseThrow(() -> new IllegalArgumentException("Node nie istnieje"));

        if (!node.isAllowPublicUse() && !node.getOwner().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Brak dostępu");
        }

        request.setProvider("COMMUNITY");
        request.setModel(node.getModelName());
        request.setUrl("tunnel://" + nodeId);
        request.setKey(node.getAuthToken());
        return request;
    }

    private RequestBuilder prepareWithSharedKey(RequestBuilder request) {
        SharedKey sharedKey = sharedKeyService
                .getRandomWorkingKeyEntity(request.getProvider());
        request.setKey(encryptionService.decrypt(sharedKey.getKeyValue()));
        request.setSharedKeyId(sharedKey.getId());
        return request;
    }

    private void blockSharedKeyIfUsed(RequestBuilder request) {
        if (request.isUseSharedKeys() && request.getSharedKeyId() != null) {
            sharedKeyRepository.findById(request.getSharedKeyId()).ifPresent(key -> {
                key.block(5);
                sharedKeyRepository.save(key);
                log.warn("Shared key {} blocked for 5 minutes due to error",
                        request.getSharedKeyId());
            });
        }
    }

    private void addPointsForSharedKey(Long sharedKeyId, int completionTokens) {
        sharedKeyRepository.findByIdWithOwner(sharedKeyId).ifPresent(key -> {
            if (key.getOwner() != null) {
                User owner = key.getOwner();
                double points = (double) completionTokens / 1000000.0;
                owner.setPoints(owner.getPoints() + points);
                userRepository.save(owner);
                log.debug(
                        "Added {} points to user {} for using shared key {} ({} completion)",
                        points, owner.getEmail(), sharedKeyId, completionTokens);
            }
        });
    }

}
