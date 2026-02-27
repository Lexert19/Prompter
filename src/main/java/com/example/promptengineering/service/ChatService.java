package com.example.promptengineering.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.entity.UserFile;
import com.example.promptengineering.exception.FileStorageException;
import com.example.promptengineering.model.Content;
import com.example.promptengineering.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import com.example.promptengineering.model.RequestBuilder;
import com.nimbusds.jose.shaded.gson.Gson;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
public class ChatService {
    private final Gson gson = new Gson();
    private final WebClient webClient;
    private final FileStorageService fileStorageService;

    public ChatService(WebClient.Builder webClientBuilder, FileStorageService fileStorageService) {
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
        this.fileStorageService = fileStorageService;
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
                .map(this::toServerSentEvent);
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
        String errorJson = "{\"error\": \"" + e.getMessage() + "\"}";
        return Flux.just(ServerSentEvent.<String>builder()
                .event("error")
                .data(errorJson)
                .build());
    }

    private boolean isImageWithFileId(Content content) {
        return "image".equals(content.getType()) && content.getFileId() != null;
    }

    public Flux<ServerSentEvent<String>> makeRequest(RequestBuilder request, User user) {
        return attachBase64Images(request, user)
                .then(buildRequestBodyJson(request))
                .flatMapMany(json -> executeRequest(request, json))
                .onErrorResume(this::handleError);
    }

}
