package com.example.promptengineering.service;

import java.time.Duration;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.promptengineering.model.RequestBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.shaded.gson.Gson;

import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Service
public class ChatService {
    private final Gson gson = new Gson();

    @Autowired
    private WebClient webClient;

    public Flux<String> makeRequest(RequestBuilder request) throws JsonProcessingException {
        String requestBodyJson = gson.toJson(request.build());
        return webClient.post()
                .uri(request.getUrl())
                .headers(httpHeaders -> configureHeaders(httpHeaders, request))
                .bodyValue(requestBodyJson)
                .retrieve()
                .bodyToFlux(String.class)
                .map(this::readChunk)
                .doOnNext(dataChunk -> {
                    // System.out.println("Received data chunk: " + dataChunk);
                })
                .retryWhen(configureRetry())
                .onErrorResume(error -> handleError(error));

    }

    private void configureHeaders(HttpHeaders headers, RequestBuilder request) {
        if (request.getProvider().equals("ANTHROPIC")) {
            headers.set("x-api-key", request.getKey());
            headers.set("Content-Type", "application/json");
            headers.set("anthropic-version", "2023-06-01");
            headers.set("anthropic-beta", "prompt-caching-2024-07-31");
        } else {
            headers.set("Authorization", "Bearer " + request.getKey());
            headers.set("Content-Type", "application/json");
        }
    }

    private Retry configureRetry() {
        return Retry.backoff(3, Duration.ofSeconds(20))
                .filter(error -> {
                    return error.getMessage() == null || error.getMessage().isEmpty();
                })
                .doBeforeRetry(signal -> System.out.println("Retrying request..."));
    }

    private Flux<String> handleError(Throwable error) {
        String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown error";
        String jsonError = gson.toJson(Collections.singletonMap("error", errorMessage));
        return Flux.just(jsonError);
    }

    private String readChunk(String chunk) {
        return chunk + "\n";
    }

}
