package com.example.promptengineering.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.promptengineering.model.RequestBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;

import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Service
public class ChatService {
    private final Gson gson = new Gson();

    @Autowired
    private WebClient webClient;

    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    public Flux<String> makeRequest(RequestBuilder request) throws JsonProcessingException {
        String requestBodyJson = gson.toJson(request.build());
        return webClient.post()
                .uri(request.getUrl())
                .headers(httpHeaders -> {
                    if(request.getProvider().equals("OPENAI")){
                        httpHeaders.set("Authorization", "Bearer " + request.getKey());
                        httpHeaders.set("Content-Type", "application/json");
                    }else if(request.getProvider().equals("ANTHROPIC")){
                        httpHeaders.set("x-api-key", request.getKey());
                        httpHeaders.set("Content-Type", "application/json");
                        httpHeaders.set("anthropic-version","2023-06-01");
                        httpHeaders.set("anthropic-beta", "prompt-caching-2024-07-31");
                    }else if(request.getProvider().equals("GEMINI")){
                        
                    }else {
                        httpHeaders.set("Authorization", "Bearer " + request.getKey());
                        httpHeaders.set("Content-Type", "application/json");
                    }
                    
                })
                .bodyValue(requestBodyJson)
                .retrieve()
                .bodyToFlux(String.class)
                .map(dataChunk -> {
                    return this.readChunk(dataChunk, request.getProvider());
                })
                .doOnNext(dataChunk -> {
                    //System.out.println("Received data chunk: " + dataChunk);
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(20))
                        .filter(error -> {
                            return error.getMessage() == null || error.getMessage().isEmpty();
                        })
                        .doBeforeRetry(signal -> System.out.println("Retrying request...")))
                .onErrorResume(error -> {
                    return Flux.just(error.getMessage() != null ? error.getMessage() : "Unknow error");
                });

    }

    private StringBuilder geminiAccumulator = new StringBuilder();
    private String geminiReadChunk(String chunk){
        geminiAccumulator.append(chunk);
        try {
            JsonNode rootNode = objectMapper.readTree(geminiAccumulator.toString());
            String content = "";
            
            if (rootNode.isArray() && rootNode.size() > 0) {
                JsonNode firstElement = rootNode.get(0);
                JsonNode candidates = firstElement.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode candidate = candidates.get(0);
                    JsonNode parts = candidate.path("content").path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        content = parts.get(0).path("text").asText();
                    }
                }
            } else {
                JsonNode candidates = rootNode.path("candidates");
                if (candidates.isArray() && candidates.size() > 0) {
                    JsonNode candidate = candidates.get(0);
                    JsonNode parts = candidate.path("content").path("parts");
                    if (parts.isArray() && parts.size() > 0) {
                        content = parts.get(0).path("text").asText();
                    }
                }
            }
            
            geminiAccumulator.setLength(0);
            return content;
        } catch (JsonProcessingException ex) {
            return "";
        }
    }

    private String readChunk(String chunk, String provider) {
        try{
            JsonNode rootNode = objectMapper.readTree(chunk);
            if(provider.equals("OPENAI")){
                String content = rootNode.path("choices").get(0).path("delta").path("content").asText();
                return content;
            } else if (provider.equals("ANTHROPIC")) {
                String content = rootNode.path("delta").path("text").asText();
                return content;
            }else if(provider.equals("GEMINI")){
                return this.geminiReadChunk(chunk);
            }else{
                String content = rootNode.path("choices").get(0).path("delta").path("content").asText();
                return content;
            }
        }catch (JsonProcessingException e){
            return "";
        }
       
    }

   
}
