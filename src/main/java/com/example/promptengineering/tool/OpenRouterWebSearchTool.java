package com.example.promptengineering.tool;

import com.example.promptengineering.model.ToolDefinition;
import com.example.promptengineering.service.ToolExecutor;
import com.example.promptengineering.service.ToolRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenRouterWebSearchTool implements ToolExecutor {

    private final ToolRegistry registry;

    @Override
    public String name() {
        return "openrouter:web_search";
    }

    @PostConstruct
    void register() {
        registry.register(new ToolDefinition(name(),
                        "OpenRouter server-side web search. The model decides when to search; "
                                + "OpenRouter performs the search and returns cited results.",
                        Map.of("type", "object",
                                "properties", Map.of(
                                        "max_results", Map.of("type", "integer",
                                                "default", 5, "minimum", 1, "maximum", 10),
                                        "engine", Map.of("type", "string",
                                                "enum", List.of("auto", "exa", "firecrawl",
                                                        "parallel", "perplexity"))),
                                "required", List.of())),
                this);
    }

    @Override
    public String execute(Map<String, Object> args) {
        throw new UnsupportedOperationException(
                "openrouter:web_search is a server-side tool executed by OpenRouter. "
                        + "It cannot be executed locally.");
    }
}