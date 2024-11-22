package com.example.promptengineering.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.promptengineering.model.ChatGPTSettings;
import com.fasterxml.jackson.core.JsonProcessingException;

import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Service
public class LlamaApiService {

    @Value("${global.nvidia.key}")
    private String apiKey;

    @Autowired
    private WebClient webClient;
    private final String CHATGPT_URL = "https://integrate.api.nvidia.com/v1/chat/completions";

    public Flux<String> makeAdminRequest(ChatGPTSettings settings, String chatgptBody)
            throws JsonProcessingException {
        settings.setApiKey(apiKey);
        return makeRequest(settings, chatgptBody);
    }

    public Flux<String> makeRequest(ChatGPTSettings settings, String chatgptBody) throws JsonProcessingException {

        System.out.println(chatgptBody);
        return webClient.post()
                .uri(CHATGPT_URL)
                .headers(httpHeaders -> {
                    httpHeaders.set("Authorization", "Bearer " + settings.getApiKey());
                    httpHeaders.set("Content-Type", "application/json");
                })
                .bodyValue(chatgptBody)
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
