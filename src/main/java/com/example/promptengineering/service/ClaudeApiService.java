package com.example.promptengineering.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.promptengineering.model.ClaudeBody;
import com.example.promptengineering.model.ClaudeSettings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Service
public class ClaudeApiService {
    @Autowired
    private ObjectMapper objectMapper;
    @Value("${global.claude.key}")
    private String apiKey;

    @Autowired
    private WebClient webClient;
    private final String CLAUDE_URL = "https://api.anthropic.com/v1/messages";

    public Flux<String> makeAdminRequest(ClaudeSettings settings, ClaudeBody claudeBody)
            throws JsonProcessingException {
        settings.setApiKey(apiKey);
        return makeRequest(settings, claudeBody);
    }

    public Flux<String> makeRequest(ClaudeSettings settings, ClaudeBody claudeBody) throws JsonProcessingException {

        String jsonBody = objectMapper.writeValueAsString(claudeBody);
        System.out.println(jsonBody);
        return webClient.post()
                .uri(CLAUDE_URL)
                .headers(httpHeaders -> {
                    httpHeaders.set("x-api-key", settings.getApiKey());
                    httpHeaders.set("Content-Type", "application/json");
                    httpHeaders.set("anthropic-version", settings.getVersion());
                    httpHeaders.set("anthropic-beta", "prompt-caching-2024-07-31");
                })
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToFlux(String.class)
                .map(dataChunk -> dataChunk + "\n")
                .doOnNext(dataChunk -> {
                    // System.out.println("Received data chunk: " + dataChunk);
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                        .filter(error -> {
                            return error.getMessage() == null || error.getMessage().isEmpty();
                        })
                        .doBeforeRetry(signal -> System.out.println("Retrying request...")))
                .onErrorResume(error -> {
                    return Flux.just(error.getMessage() != null ? error.getMessage() : "Unknow error");
                });

    }
}
