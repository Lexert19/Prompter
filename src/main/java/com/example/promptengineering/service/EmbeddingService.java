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
import com.example.promptengineering.model.FileElement;
import com.example.promptengineering.repository.ProjectRepository;

@Service
public class EmbeddingService {
    private static final String EMBEDDINGS_URL = "https://api.openai.com/v1/embeddings";

    @Autowired
    private WebClient webClient;
    @Autowired
    private ProjectRepository projectRepository;

    public void addFileToProject(Project project, FileElement file, User user) {
        String apiKey = user.getKeys().getOrDefault("OPENAI", "");
        List<String> pages = splitContentIntoPages(file.getContent());
        file.setPages(pages);
        createEmbeddingForFile(file, apiKey);
        project.getFiles().add(file);
        projectRepository.save(project);
    }

    private void createEmbeddingForFile(FileElement file, String apiKey) {
        List<String> pages = file.getPages();
        if (pages == null || pages.isEmpty()) {
            return;
        }

        List<List<Double>> vectors = new ArrayList<>();
        for (String page : pages) {
            List<Double> vector = getEmbedding(page, apiKey);
            vectors.add(vector);
        }
        file.setVectors(vectors);
    }

    public List<Double> getEmbedding(String text, String apiKey) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", text);
        requestBody.put("model", "text-embedding-ada-002");

        ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<>() {
        };
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

        List<FileElement> projectFiles = project.getFiles();
        if (projectFiles == null || projectFiles.isEmpty()) {
            return new ArrayList<>();
        }

        List<ScoredFragment> scoredFragments = new ArrayList<>();
        for (FileElement file : projectFiles) {
            List<String> pages = file.getPages();
            List<List<Double>> vectors = file.getVectors();
            if (pages == null || vectors == null || pages.size() != vectors.size()) {
                continue;
            }
            for (int i = 0; i < pages.size(); i++) {
                String page = pages.get(i);
                List<Double> vector = vectors.get(i);
                if (vector == null || vector.isEmpty())
                    continue;
                double similarity = cosineSimilarity(queryVector, vector);
                scoredFragments.add(new ScoredFragment(page, similarity));
            }
        }

        scoredFragments.sort((a, b) -> Double.compare(b.score, a.score));

        int topN = 5;
        return scoredFragments.stream()
                .limit(topN)
                .map(ScoredFragment::getText)
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

    private List<String> splitContentIntoPages(String content) {
        List<String> pages = new ArrayList<>();
        int fragmentSize = 512;
        for (int i = 0; i < content.length(); i += fragmentSize) {
            int end = Math.min(i + fragmentSize, content.length());
            String fragment = content.substring(i, end);
            if (end < content.length() && Character.isLetter(content.charAt(end - 1))) {
                while (end > i && content.charAt(end - 1) != ' ') {
                    end--;
                }
                if (end == i) {
                    end = i + fragmentSize;
                }
            }
            pages.add(fragment);
        }
        return pages;
    }

    private static class ScoredFragment {
        private String text;
        private double score;
        
        public ScoredFragment(String text, double score) {
            this.text = text;
            this.score = score;
        }
    
        public String getText() {
            return text;
        }
    }

}
