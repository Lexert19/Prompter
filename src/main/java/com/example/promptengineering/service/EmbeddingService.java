package com.example.promptengineering.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.promptengineering.entity.Project;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.Embedding;
import com.example.promptengineering.model.FileElement;

@Service
public class EmbeddingService {
    private static final String EMBEDDINGS_URL = "https://api.openai.com/v1/embeddings";

    @Autowired
    private WebClient webClient;


     public void createProjectEmbedding(Project project, User user) {
        String apiKey = user.getChatgptKey();
        List<FileElement> files = project.getFiles();
        
        if (files == null || files.isEmpty()) {
            return; 
        }

        List<Embedding> embeddings = new ArrayList<>();
        for (FileElement file : files) {
            String content = file.getContent();
            List<Double> vector = getEmbedding(content, apiKey);
            
            Embedding embedding = new Embedding();
            embedding.setVector(vector);
            embedding.setName(file.getName());
            embeddings.add(embedding);
        }
        project.setEmbeddings(embeddings);
    }

    public List<Double> getEmbedding(String text, String apiKey) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", text);
        requestBody.put("model", "text-embedding-ada-002");

        Map<String, Object> responseBody = webClient.post()
            .uri(EMBEDDINGS_URL)
            .header("Authorization", "Bearer " + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Map.class)
            .block();

        if (responseBody != null && responseBody.containsKey("data")) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
            if (!data.isEmpty()) {
                return (List<Double>) data.get(0).get("embedding");
            }
        }
        throw new RuntimeException("Failed to get embedding for text: " + text);
    }
}
