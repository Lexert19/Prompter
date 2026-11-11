package com.example.promptengineering.tool;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.ToolDefinition;
import com.example.promptengineering.service.ToolExecutor;
import com.example.promptengineering.service.ToolRegistry;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class WebSearchTool implements ToolExecutor {
    public static final String TOOL_PROVIDER = "TAVILY";

    private static final int DEFAULT_MAX_RESULTS = 5;
    private static final int MAX_RESULTS_LIMIT = 10;
    private static final int SNIPPET_MAX_CHARS = 300;

    private final ToolRegistry registry;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final String baseUrl;

    public WebSearchTool(ToolRegistry registry, WebClient.Builder webClientBuilder,
            ObjectMapper objectMapper, UserService userService,
            @Value("${tools.search.base-url:https://api.tavily.com}") String baseUrl) {
        this.registry = registry;
        this.objectMapper = objectMapper;
        this.userService = userService;
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public String name() {
        return "web_search";
    }

    @PostConstruct
    void register() {
        registry.register(new ToolDefinition(name(),
                "Searches the web. Returns top results: title, url, snippet.",
                Map.of("type", "object", "properties",
                        Map.of("query",
                                Map.of("type", "string", "description", "search query"),
                                "max_results",
                                Map.of("type", "integer", "default", DEFAULT_MAX_RESULTS,
                                        "minimum", 1, "maximum", MAX_RESULTS_LIMIT)),
                        "required", List.of("query"))),
                this);
    }

    @Override
    public String execute(Map<String, Object> args) throws Exception {
        throw new IllegalStateException(
                "web_search requires a user context to resolve the API key");
    }

    @Override
    public String execute(Map<String, Object> args, User user) throws Exception {
        Object rawQuery = args.get("query");
        if (rawQuery == null || rawQuery.toString().isBlank()) {
            throw new IllegalArgumentException("web_search: missing 'query'");
        }
        String query = rawQuery.toString();
        int max = clampMaxResults(args.get("max_results"));

        String apiKey = resolveApiKey(user);
        if (apiKey.isBlank()) {
            throw new IllegalStateException("web_search: missing API key. Add your "
                    + TOOL_PROVIDER + " key in account settings.");
        }

        var body = Map.of("query", query, "max_results", max, "include_answer", false,
                "include_raw_content", false);

        String json = webClient.post().uri(baseUrl + "/search")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON).bodyValue(body).retrieve()
                .bodyToMono(String.class).block(Duration.ofSeconds(15));

        if (json == null || json.isBlank()) {
            throw new IllegalStateException("web_search: empty response from provider");
        }

        JsonNode root = objectMapper.readTree(json);
        var results = new ArrayList<Map<String, Object>>();
        for (JsonNode r : root.path("results")) {
            results.add(Map.of("title", r.path("title").asText(""), "url",
                    r.path("url").asText(""), "snippet",
                    truncate(r.path("content").asText(""))));
        }
        return objectMapper
                .writeValueAsString(Map.of("query", query, "results", results));
    }

    private String resolveApiKey(User user) {
        if (user == null) {
            throw new IllegalStateException(
                    "web_search: missing user context for API key resolution");
        }
        String userKey = userService.getUserKeys(user).get(TOOL_PROVIDER);
        if (userKey == null || userKey.isBlank()) {
            throw new IllegalStateException("web_search: missing API key for "
                    + TOOL_PROVIDER + ". Add your key in account settings.");
        }
        return userKey;
    }

    private static int clampMaxResults(Object raw) {
        if (raw == null)
            return DEFAULT_MAX_RESULTS;
        int v = ((Number) raw).intValue();
        return Math.clamp(v, 1, MAX_RESULTS_LIMIT);
    }

    private static String truncate(String s) {
        if (s == null)
            return "";
        return s.length() <= WebSearchTool.SNIPPET_MAX_CHARS
                ? s
                : s.substring(0, WebSearchTool.SNIPPET_MAX_CHARS);
    }
}
