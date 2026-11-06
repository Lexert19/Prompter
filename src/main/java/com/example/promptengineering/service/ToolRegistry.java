package com.example.promptengineering.service;

import com.example.promptengineering.model.ToolDefinition;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ToolRegistry {

    private final Map<String, ToolDefinition> definitions = new ConcurrentHashMap<>();
    private final Map<String, ToolExecutor> executors = new ConcurrentHashMap<>();

    public ToolRegistry() {
    }

    public void register(ToolDefinition def) {
        definitions.put(def.name(), def);
    }

    public void register(ToolDefinition def, ToolExecutor exec) {
        definitions.put(def.name(), def);
        executors.put(def.name(), exec);
    }

    public Optional<ToolExecutor> getExecutor(String name) {
        return Optional.ofNullable(executors.get(name));
    }

    public List<ToolDefinition> resolve(List<String> enabled) {
        if (enabled == null || enabled.isEmpty()) {
            return List.copyOf(definitions.values());
        }
        return enabled.stream().map(definitions::get).filter(Objects::nonNull).toList();
    }

    public Collection<ToolDefinition> all() {
        return definitions.values();
    }
}
