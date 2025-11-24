package com.example.promptengineering.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.promptengineering.entity.FileElement;
import com.example.promptengineering.entity.Project;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.ScoredFragment;
import com.example.promptengineering.repository.FileElementsRepository;
import com.example.promptengineering.repository.ProjectRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmbeddingService {
    private static final String EMBEDDINGS_URL = "https://api.openai.com/v1/embeddings";

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FileElementsRepository fileElementRepository;

    public void addFileToProject(Project project, FileElement file, User user) {
        String apiKey = user.getKeys().getOrDefault("OPENAI", "");
        List<String> pages = splitContentIntoPages(file.getContent());
        file.setPages(pages);
        file.setProject(project);
        file.setUser(user);

        FileElement fileElement = createEmbeddingForFile(file, apiKey);
        fileElementRepository.save(fileElement);
    }

    private FileElement createEmbeddingForFile(FileElement file, String apiKey) {
        List<String> pages = file.getPages();
        if (pages == null || pages.isEmpty()) {
            return file;
        }

        List<List<Double>> vectors = new ArrayList<>();
        for (String page : pages) {
            List<Double> vector = getEmbedding(page, apiKey);
            vectors.add(vector);
        }
        file.setVectors(vectors);
        return file;
    }

    public List<Double> getEmbedding(String text, String apiKey) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", text);
        requestBody.put("model", "text-embedding-3-large");

        Map<String, Object> response = restTemplate.postForObject(EMBEDDINGS_URL,
                getHttpEntity(requestBody, apiKey), Map.class);

        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (!data.isEmpty()) {
            return (List<Double>) data.get(0).get("embedding");
        }
        throw new RuntimeException("Failed to get embedding for text: " + text);
    }

    private org.springframework.http.HttpEntity<Map<String, Object>> getHttpEntity(Map<String, Object> requestBody, String apiKey) {
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        return new org.springframework.http.HttpEntity<>(requestBody, headers);
    }

    public List<ScoredFragment> retrieveSimilarFragments(String query, Project project, User user) {
        String apiKey = user.getKeys().getOrDefault("OPENAI", "");

        List<Double> queryVector = getEmbedding(query, apiKey);
        return processQueryVectorWithProjectFiles(queryVector, project);
    }

    private List<ScoredFragment> processQueryVectorWithProjectFiles(List<Double> queryVector, Project project) {
        List<FileElement> fileElements = fileElementRepository.findByProject(project);
        List<ScoredFragment> scoredFragments = new ArrayList<>();

        for (FileElement fileElement : fileElements){
            scoredFragments.addAll(processFile(fileElement, queryVector));
        }
        return formatTopFiveResults(scoredFragments);
    }

    private List<ScoredFragment> processFile(FileElement file, List<Double> queryVector) {
        List<String> pages = file.getPages();
        List<List<Double>> vectors = file.getVectors();

        if (pages == null || vectors == null || pages.size() != vectors.size()) {
            return List.of();
        }

        List<ScoredFragment> scoredFragments = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            ScoredFragment scoredFragment = processPage(pages, vectors, i, queryVector);
            if (scoredFragment != null) {
                scoredFragments.add(scoredFragment);
            }
        }
        return scoredFragments;
    }

    private ScoredFragment processPage(List<String> pages, List<List<Double>> vectors, int index,
                                       List<Double> queryVector) {
        String page = pages.get(index);
        List<Double> vector = vectors.get(index);

        if (vector == null || vector.isEmpty()) {
            return null;
        }

        double similarity = cosineSimilarity(queryVector, vector);
        return new ScoredFragment(page, similarity);
    }

    private List<ScoredFragment> formatTopFiveResults(List<ScoredFragment> scoredFragments) {
        scoredFragments.sort(Comparator.comparingDouble(ScoredFragment::getScore).reversed());
        return scoredFragments.stream()
                .limit(5)
                .collect(Collectors.toList());
    }

  

    private double cosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must have the same length");
        }
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        normA = Math.sqrt(normA);
        normB = Math.sqrt(normB);
        if (normA == 0 || normB == 0) {
            return 0.0; 
        }
        return dotProduct / (normA * normB);
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

    

}
