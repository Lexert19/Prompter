package com.example.promptengineering.service;

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
import com.example.promptengineering.model.Message;
import com.example.promptengineering.repository.SharedKeyRepository;
import com.example.promptengineering.repository.UserRepository;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import com.example.promptengineering.model.RequestBuilder;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
public class ChatService {
    private final Gson gson;
    private final WebClient webClient;
    private final FileStorageService fileStorageService;
    private final SharedKeyService sharedKeyService;
    private final EncryptionService encryptionService;
    private final SharedKeyRepository sharedKeyRepository;
    private final UserRepository userRepository;
    private final TokenTrackingService tokenTrackingService;

    public ChatService(Gson gson, WebClient.Builder webClientBuilder, FileStorageService fileStorageService, SharedKeyService sharedKeyService, EncryptionService encryptionService, SharedKeyRepository sharedKeyRepository, UserRepository userRepository, TokenTrackingService tokenTrackingService) {
        this.gson = gson;
        this.webClient = webClientBuilder
                .codecs(configure -> configure.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
        this.fileStorageService = fileStorageService;
        this.sharedKeyService = sharedKeyService;
        this.encryptionService = encryptionService;
        this.sharedKeyRepository = sharedKeyRepository;
        this.userRepository = userRepository;
        this.tokenTrackingService = tokenTrackingService;
    }

    private Mono<Void> attachBase64Images(RequestBuilder request, User user) {
        return Mono.fromRunnable(() -> {
            for (Message msg : request.getMessages()) {
                for (Content content : msg.getContent()) {
                    if (isImageWithFileId(content)) {
                        enrichContentWithBase64(content, user);
                    }
                }
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    private void enrichContentWithBase64(Content content, User user) {
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
        return Mono.fromCallable(() -> gson.toJson(request.build()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Flux<ServerSentEvent<String>> executeRequest(RequestBuilder request, String json) {
        WebClient.RequestBodySpec requestSpec = buildHttpRequest(request, json);


        return requestSpec.retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofMinutes(5))
                .filter(line -> !line.isBlank())
                .doFinally(signalType -> {
                    if (signalType == SignalType.ON_COMPLETE && request.getSharedKeyId() != null) {
                        int completion = tokenTrackingService.getCompletionTokens();
                        addPointsForSharedKey(request.getSharedKeyId(), completion);
                    }
                })
                .map(this::toServerSentEvent)
                .doOnCancel(() -> log.debug("SSE stream cancelled by client"))
                .onErrorResume(this::handleError);
    }

    private WebClient.RequestBodySpec buildHttpRequest(RequestBuilder request, String json) {
        WebClient.RequestBodySpec spec = (WebClient.RequestBodySpec) webClient.post()
                .uri(request.getUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json);

        if (request.getProvider().equals("ANTHROPIC")) {
            spec.header("x-api-key", request.getKey())
                    .header("anthropic-version", "2023-06-01")
                    .header("anthropic-beta", "prompt-caching-2024-07-31");
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
        String errorJson = gson.toJson(errorMap);
        log.debug("API request error: {}", errorJson);

        return Flux.just(ServerSentEvent.<String>builder()
                .event("error")
                .data(errorJson)
                .build());
    }

    private boolean isImageWithFileId(Content content) {
        return "image".equals(content.getType()) && content.getFileId() != null;
    }

    public Flux<ServerSentEvent<String>> makeRequest(RequestBuilder request, User user) {
        Mono<RequestBuilder> preparedRequest = request.isUseSharedKeys()
                ? Mono.fromCallable(() -> prepareWithSharedKey(request))
                .subscribeOn(Schedulers.boundedElastic())
                : Mono.just(request);

        return preparedRequest
                .flatMapMany(req -> attachBase64Images(req, user)
                        .then(buildRequestBodyJson(req))
                        .flatMapMany(json -> executeRequest(req, json))
                )
                .doOnError(e -> blockSharedKeyIfUsed(request))
                .onErrorResume(this::handleError);
    }

    private RequestBuilder prepareWithSharedKey(RequestBuilder request) {
        SharedKey sharedKey = sharedKeyService.getRandomWorkingKeyEntity(request.getProvider());
        request.setKey(encryptionService.decrypt(sharedKey.getKeyValue()));
        request.setSharedKeyId(sharedKey.getId());
        return request;
    }

    private void blockSharedKeyIfUsed(RequestBuilder request) {
        if (request.isUseSharedKeys() && request.getSharedKeyId() != null) {
            sharedKeyRepository.findById(request.getSharedKeyId()).ifPresent(key -> {
                key.block(1);
                sharedKeyRepository.save(key);
                log.warn("Shared key {} blocked for 5 minutes due to error", request.getSharedKeyId());
            });
        }
    }

    private void addPointsForSharedKey(Long sharedKeyId, int completionTokens) {
        sharedKeyRepository.findByIdWithOwner(sharedKeyId).ifPresent(key -> {
            if (key.getOwner() != null) {
                User owner = key.getOwner();
                double points = (double) completionTokens / 1000.0;
                owner.setPoints(owner.getPoints() + points);
                userRepository.save(owner);
                log.debug("Added {} points to user {} for using shared key {} ({} completion)",
                        points, owner.getEmail(), sharedKeyId, completionTokens);
            }
        });
    }

}
