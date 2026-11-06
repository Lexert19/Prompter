package com.example.promptengineering.service;

import com.example.promptengineering.model.ToolCall;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ToolCallAccumulator {

    public record Chunk(String content, String reasoning, String finishReason,
            String error) {
        public boolean hasContent() {
            return content != null && !content.isEmpty();
        }
        public boolean hasReasoning() {
            return reasoning != null && !reasoning.isEmpty();
        }
        public boolean hasError() {
            return error != null && !error.isEmpty();
        }

        public static Chunk of(String c, String r, String f) {
            return new Chunk(c, r, f, null);
        }
        public static Chunk error(String msg) {
            return new Chunk(null, null, "error", msg);
        }
    }

    private final ObjectMapper mapper;
    private final Map<Integer, Partial> partials = new TreeMap<>();
    private final StringBuilder content = new StringBuilder();
    private final StringBuilder reasoning = new StringBuilder();
    private String finishReason;

    private int reportedReasoningTokens = -1;
    private int reportedCompletionTokens = -1;

    public ToolCallAccumulator(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Chunk consume(String data) {
        if (data == null)
            return Chunk.of(null, null, null);
        String d = data.trim();
        if (d.isEmpty() || "[DONE]".equals(d))
            return Chunk.of(null, null, null);

        String cDelta = null, rDelta = null;
        try {
            JsonNode root = mapper.readTree(d);

            if (root.hasNonNull("error")) {
                JsonNode err = root.get("error");
                String msg = err.hasNonNull("message")
                        ? err.get("message").asText()
                        : err.toString();
                return Chunk.error(msg);
            }

            captureUsage(root);

            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.isEmpty()) {
                if (root.hasNonNull("usage")) {
                    return Chunk.of(null, null, null);
                }
                log.warn("Unexpected SSE chunk (no error, no choices): {}", d);
                return Chunk.error("Unexpected provider response: " + d);
            }

            JsonNode choice = choices.get(0);
            JsonNode delta = choice.path("delta");

            if (delta.hasNonNull("content")) {
                String c = delta.get("content").asText();
                if (!c.isEmpty()) {
                    cDelta = c;
                    content.append(c);
                }
            }

            rDelta = extractReasoningDelta(delta);

            JsonNode finish = choice.path("finish_reason");
            if (!finish.isNull() && !finish.isMissingNode()) {
                finishReason = finish.asText();
            }

            JsonNode calls = delta.path("tool_calls");
            if (calls.isArray()) {
                for (JsonNode tc : calls) {
                    int idx = tc.path("index").asInt(0);
                    Partial p = partials.computeIfAbsent(idx, i -> new Partial());
                    if (tc.hasNonNull("id"))
                        p.id = tc.get("id").asText();
                    JsonNode fn = tc.path("function");
                    if (fn.hasNonNull("name"))
                        p.name = fn.get("name").asText();
                    if (fn.hasNonNull("arguments"))
                        p.args.append(fn.get("arguments").asText());
                }
            }
            return Chunk.of(cDelta, rDelta, finishReason);
        } catch (Exception e) {
            log.warn("Malformed SSE chunk: {}", d, e);
            return Chunk.error("Malformed chunk: " + e.getMessage());
        }
    }

    private String extractReasoningDelta(JsonNode delta) {
        if (delta.hasNonNull("reasoning_content")) {
            String r = delta.get("reasoning_content").asText();
            if (!r.isEmpty()) {
                reasoning.append(r);
                return r;
            }
        }

        if (delta.hasNonNull("reasoning")) {
            String r = delta.get("reasoning").asText();
            if (!r.isEmpty()) {
                reasoning.append(r);
                return r;
            }
        }

        JsonNode details = delta.path("reasoning_details");
        if (details.isArray() && !details.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode d : details) {
                if (d.hasNonNull("text")) {
                    sb.append(d.get("text").asText());
                } else if (d.hasNonNull("summary")) {
                    sb.append(d.get("summary").asText());
                }
            }
            if (sb.length() > 0) {
                String r = sb.toString();
                reasoning.append(r);
                return r;
            }
        }

        if (delta.hasNonNull("thinking")) {
            String r = delta.get("thinking").asText();
            if (!r.isEmpty()) {
                reasoning.append(r);
                return r;
            }
        }

        return null;
    }

    private void captureUsage(JsonNode root) {
        JsonNode usage = root.path("usage");
        if (!usage.isObject())
            return;

        if (usage.hasNonNull("completion_tokens")) {
            reportedCompletionTokens = usage.get("completion_tokens").asInt(-1);
        }
        JsonNode details = usage.path("completion_tokens_details");
        if (details.isObject() && details.hasNonNull("reasoning_tokens")) {
            reportedReasoningTokens = details.get("reasoning_tokens").asInt(-1);
            if (reportedReasoningTokens > 0) {
                log.debug("Reported reasoning_tokens from provider: {}",
                        reportedReasoningTokens);
            }
        }
    }

    public List<ToolCall> completedCalls() {
        return partials.values().stream()
                .filter(p -> p.id != null && !p.id.isBlank() && p.name != null
                        && !p.name.isBlank())
                .map(p -> new ToolCall(p.id, p.name, p.args.toString()))
                .collect(Collectors.toList());
    }

    public boolean hasToolCalls() {
        return !partials.isEmpty();
    }
    public String getContent() {
        return content.toString();
    }
    public String getReasoning() {
        return reasoning.toString();
    }
    public String getFinishReason() {
        return finishReason;
    }

    public int getReportedReasoningTokens() {
        return reportedReasoningTokens;
    }
    public int getReportedCompletionTokens() {
        return reportedCompletionTokens;
    }

    private static class Partial {
        String id;
        String name;
        final StringBuilder args = new StringBuilder();
    }
}
