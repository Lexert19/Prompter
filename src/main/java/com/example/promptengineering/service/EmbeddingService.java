package com.example.promptengineering.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
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
        String apiKey = user.getKeys().getOrDefault("OPENAI", "");
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

        ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<>() {};
        Map<String, Object> responseBody = webClient.post()
            .uri(EMBEDDINGS_URL)
            .header("Authorization", "Bearer " + apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(typeRef)
            .block();

        if (responseBody != null && responseBody.containsKey("data")) {
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
            if (!data.isEmpty()) {
                return (List<Double>) data.get(0).get("embedding");
            }
        }
        throw new RuntimeException("Failed to get embedding for text: " + text);
    }

    public List<String> retrieveSimilarFragments(String query, Project project, User user) {
        String apiKey = user.getKeys().getOrDefault("OPENAI", "");
        List<Double> queryVector = getEmbedding(query, apiKey);
        
        List<Embedding> projectEmbeddings = project.getEmbeddings();
        if (projectEmbeddings == null || projectEmbeddings.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, FileElement> fileNameToFileMap = new HashMap<>();
        project.getFiles().forEach(file -> fileNameToFileMap.put(file.getName(), file));
        
        List<ScoredEmbedding> scoredEmbeddings = new ArrayList<>();
        for (Embedding emb : projectEmbeddings) {
            if (emb.getVector() == null || emb.getVector().isEmpty()) continue;
            double similarity = cosineSimilarity(queryVector, emb.getVector());
            scoredEmbeddings.add(new ScoredEmbedding(emb, similarity));
        }
        
        scoredEmbeddings.sort((a, b) -> Double.compare(b.score, a.score));
        
        int topN = 5;
        return scoredEmbeddings.stream()
                .limit(topN)
                .map(se -> fileNameToFileMap.get(se.embedding.getName()))
                .filter(Objects::nonNull)
                .map(FileElement::getContent)
                .collect(Collectors.toList());
    }

    private double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
    
    private static class ScoredEmbedding {
        Embedding embedding;
        double score;
        
        ScoredEmbedding(Embedding embedding, double score) {
            this.embedding = embedding;
            this.score = score;
        }
    }
    
}
