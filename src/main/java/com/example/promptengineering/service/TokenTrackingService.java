package com.example.promptengineering.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class TokenTrackingService {

    public static class TokenState {
        private final StringBuilder accumulatedText = new StringBuilder();
        private final AtomicInteger completionTokens = new AtomicInteger(0);
        private final AtomicInteger reasoningTokens = new AtomicInteger(0);
        private boolean usageReceived = false;
    }

    public TokenState createState() {
        return new TokenState();
    }

    public void processChunk(String line, String provider, TokenState state) {
        String data = line;
        if (data.startsWith("data: ")) {
            data = data.substring(6);
        }
        try {
            JsonObject json = JsonParser.parseString(data).getAsJsonObject();
            handleOpenAIChunk(json, state);
//            if ("OPENAI".equals(provider)) {
//
//            } else if ("ANTHROPIC".equals(provider)) {
//                handleAnthropicChunk(json, state);
//            } else if ("GEMINI".equals(provider)) {
//                handleGeminiChunk(json, state);
//            }
        } catch (Exception e) {
            log.debug("Failed to parse chunk: {}", line);
        }
    }

    private void handleOpenAIChunk(JsonObject json, TokenState state) {
        if (json.has("choices") && json.getAsJsonArray("choices").size() > 0) {
            JsonObject choice = json.getAsJsonArray("choices").get(0).getAsJsonObject();
            if (choice.has("delta")) {
                JsonObject delta = choice.getAsJsonObject("delta");
                if (delta.has("content") && !delta.get("content").isJsonNull()) {
                    state.accumulatedText.append(delta.get("content").getAsString());
                }
            }
        }
        if (json.has("usage")) {
            JsonObject usage = json.getAsJsonObject("usage");
            if (usage.has("completion_tokens")) {
                state.completionTokens.set(usage.get("completion_tokens").getAsInt());
            }
            if (usage.has("reasoning_tokens")) {
                state.reasoningTokens.set(usage.get("reasoning_tokens").getAsInt());
            }
            state.usageReceived = true;
        }
    }

    private void handleAnthropicChunk(JsonObject json, TokenState state) {
        if (json.has("type") && "content_block_delta".equals(json.get("type").getAsString())) {
            JsonObject delta = json.getAsJsonObject("delta");
            if (delta.has("text")) {
                state.accumulatedText.append(delta.get("text").getAsString());
            }
        }
    }

    private void handleGeminiChunk(JsonObject json, TokenState state) {
        if (json.has("candidates") && json.getAsJsonArray("candidates").size() > 0) {
            JsonObject candidate = json.getAsJsonArray("candidates").get(0).getAsJsonObject();
            if (candidate.has("content") && candidate.getAsJsonObject("content").has("parts")) {
                var parts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                for (var part : parts) {
                    JsonObject partObj = part.getAsJsonObject();
                    if (partObj.has("text")) {
                        state.accumulatedText.append(partObj.get("text").getAsString());
                    }
                }
            }
        }
        if (json.has("usageMetadata")) {
            JsonObject usage = json.getAsJsonObject("usageMetadata");
            if (usage.has("candidatesTokenCount")) {
                state.completionTokens.set(usage.get("candidatesTokenCount").getAsInt());
            }
            state.usageReceived = true;
        }
    }

    public int getCompletionTokens(TokenState state) {
        return state.usageReceived ? state.completionTokens.get() : 0;
    }

    public int getReasoningTokens(TokenState state) {
        return state.reasoningTokens.get();
    }
}