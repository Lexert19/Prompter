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
import org.springframework.web.reactive.function.client.WebClient;

import com.example.promptengineering.entity.FileElement;
import com.example.promptengineering.entity.Project;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.FileElementsRepository;
import com.example.promptengineering.repository.ProjectRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class EmbeddingService {
    private static final String EMBEDDINGS_URL = "https://api.openai.com/v1/embeddings";

    @Autowired
    private WebClient webClient;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private FileElementsRepository fileElementRepository;

    public Mono<Void> addFileToProject(Project project, FileElement file, User user) {
        String apiKey = user.getKeys().getOrDefault("OPENAI", "");
        List<String> pages = splitContentIntoPages(file.getContent());
        file.setPages(pages);
        file.setProject(project.getId());
        file.setUserId(user.getId());

        return createEmbeddingForFile(file, apiKey)
                .flatMap(f -> fileElementRepository.save(f))
                .then();
    }

    private Mono<FileElement> createEmbeddingForFile(FileElement file, String apiKey) {
        List<String> pages = file.getPages();
        if (pages == null || pages.isEmpty()) {
            return Mono.just(file);
        }

        return Flux.fromIterable(pages)
                .flatMap(page -> getEmbedding(page, apiKey))
                .collectList()
                .map(vectors -> {
                    file.setVectors(vectors);
                    return file;
                });
    }

    public Mono<List<Double>> getEmbedding(String text, String apiKey) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", text);
        requestBody.put("model", "text-embedding-ada-002");

        ParameterizedTypeReference<Map<String, Object>> typeRef = new ParameterizedTypeReference<>() {
        };

        return webClient.post()
                .uri(EMBEDDINGS_URL)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(typeRef)
                .map(responseBody -> {
                    List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
                    if (!data.isEmpty()) {
                        return (List<Double>) data.get(0).get("embedding");
                    }
                    throw new RuntimeException("Failed to get embedding for text: " + text);
                });
    }

    public Mono<List<String>> retrieveSimilarFragments(String query, Project project, User user) {
        String apiKey = user.getKeys().getOrDefault("OPENAI", "");

        return getEmbedding(query, apiKey)
                .flatMap(queryVector -> processQueryVectorWithProjectFiles(queryVector, project))
                .flatMap(this::formatTopFiveResults);
    }

    private Mono<List<ScoredFragment>> processQueryVectorWithProjectFiles(List<Double> queryVector, Project project) {
        return fileElementRepository.findByProject(project.getId())
                .flatMap(file -> processFile(file, queryVector))
                .collectList();
    }

    private Flux<ScoredFragment> processFile(FileElement file, List<Double> queryVector) {
        List<String> pages = file.getPages();
        List<List<Double>> vectors = file.getVectors();

        if (pages == null || vectors == null || pages.size() != vectors.size()) {
            return Flux.empty();
        }

        return Flux.range(0, pages.size())
                .flatMap(index -> processPage(pages, vectors, index, queryVector));
    }

    private Mono<ScoredFragment> processPage(List<String> pages, List<List<Double>> vectors, int index,
            List<Double> queryVector) {
        String page = pages.get(index);
        List<Double> vector = vectors.get(index);

        if (vector == null || vector.isEmpty()) {
            return Mono.empty();
        }

        double similarity = euclideanDistance(queryVector, vector);
        return Mono.just(new ScoredFragment(page, similarity));
    }

    private Mono<List<String>> formatTopFiveResults(List<ScoredFragment> scoredFragments) {
        return Mono.just(scoredFragments)
                .map(list -> {
                    list.sort(Comparator.comparingDouble(ScoredFragment::getScore).reversed());
                    return list.stream()
                            .limit(5)
                            .map(ScoredFragment::getText)
                            .collect(Collectors.toList());
                });
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

    private double euclideanDistance(List<Double> vectorA, List<Double> vectorB) {
        double sum = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            sum += Math.pow(vectorA.get(i) - vectorB.get(i), 2);
        }
        return Math.sqrt(sum);
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
        public double getScore() { 
            return score;
        }

    }

}
