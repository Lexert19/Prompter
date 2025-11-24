package com.example.promptengineering.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.stream.Stream;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;

import com.example.promptengineering.model.RequestBuilder;
import com.nimbusds.jose.shaded.gson.Gson;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@Service
public class ChatService {
    private final Gson gson = new Gson();
    private final WebClient webClient;

    public ChatService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    public Flux<ServerSentEvent<String>> makeRequest(RequestBuilder request) {
        String requestBodyJson = gson.toJson(request.build());

        WebClient.RequestBodySpec requestSpec = webClient.post()
                .uri(request.getUrl())
                .contentType(MediaType.APPLICATION_JSON);

        if ("ANTHROPIC".equals(request.getProvider())) {
            requestSpec.header("x-api-key", request.getKey())
                    .header("anthropic-version", "2023-06-01")
                    .header("anthropic-beta", "prompt-caching-2024-07-31");
        } else {
            requestSpec.header("Authorization", "Bearer " + request.getKey());
        }

        return requestSpec.bodyValue(requestBodyJson)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(Duration.ofMinutes(5))
                .filter(line -> !line.isBlank())
                .map(line -> ServerSentEvent.<String>builder()
                        .data(line)
                        .build())
                .onErrorResume(e -> Flux.just(
                        ServerSentEvent.<String>builder()
                                .event("error")
                                .data("{\"error\": \"" + e.getMessage() + "\"}")
                                .build()
                ));
    }
}
