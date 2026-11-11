package com.example.promptengineering.service;

import com.example.promptengineering.entity.SharedKey;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.Message;
import com.example.promptengineering.model.RequestBuilder;
import com.example.promptengineering.model.ToolDefinition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatRequestFactory {

    private final UserService userService;
    private final SharedKeyService sharedKeyService;
    private final EncryptionService encryptionService;
    private final ToolRegistry toolRegistry;
    private final SessionMessageMapper messageMapper;

    public RequestBuilder build(ChatSession session, User user) {
        requireModel(session);
        requireUrl(session);

        RequestBuilder builder = new RequestBuilder();
        applyBasics(session, builder);
        applyAdvancedParams(session, builder);
        applyMessages(session, builder);
        applyApiKey(session, user, builder);
        applyTools(session, builder);
        return builder;
    }

    private void requireModel(ChatSession session) {
        if (session.getModel() == null)
            throw new IllegalStateException("Model not set");
    }

    private void requireUrl(ChatSession session) {
        if (session.getUrl() == null)
            throw new IllegalStateException("URL not set");
    }

    private void applyBasics(ChatSession session, RequestBuilder builder) {
        builder.setModel(session.getModel());
        builder.setUrl(session.getUrl());
        builder.setStream(true);
        builder.setProvider(session.getProvider());
        if (session.getProviderStrategy() != null) {
            builder.setProviderStrategy(session.getProviderStrategy());
        }
        if (session.getSystemPrompt() != null) {
            builder.setSystem(session.getSystemPrompt());
        }
    }

    private void applyAdvancedParams(ChatSession session, RequestBuilder builder) {
        boolean anyAdvanced = false;
        if (session.getTemperature() != null) {
            builder.setTemperature(session.getTemperature());
            anyAdvanced = true;
        }
        if (session.getMaxTokens() != null) {
            builder.setMaxTokens(session.getMaxTokens());
            anyAdvanced = true;
        }
        if (session.getTopP() != null) {
            builder.setTop_p(session.getTopP());
            anyAdvanced = true;
        }
        if (session.getFrequencyPenalty() != null) {
            builder.setFrequencyPenalty(session.getFrequencyPenalty());
            anyAdvanced = true;
        }
        if (session.getPresencePenalty() != null) {
            builder.setPresencePenalty(session.getPresencePenalty());
            anyAdvanced = true;
        }
        if (session.getReasoningEffort() != null) {
            builder.setReasoningEffort(session.getReasoningEffort());
        }
        if (session.getProviderConfig() != null) {
            builder.setProviderConfig(session.getProviderConfig());
        }
        builder.includeAdvancedParams(anyAdvanced);
    }

    private void applyMessages(ChatSession session, RequestBuilder builder) {
        List<Message> messages = session.getMessages().stream()
                .map(messageMapper::toProviderMessage)
                .collect(Collectors.toCollection(ArrayList::new));
        builder.setMessages(messages);
    }

    private void applyApiKey(ChatSession session, User user, RequestBuilder builder) {
        String providerKey = session.getProvider().toUpperCase();
        String apiKey;
        if (session.isUseSharedKeys()) {
            SharedKey sk = sharedKeyService.getRandomWorkingKeyEntity(providerKey)
                    .orElseThrow(() -> new IllegalStateException(
                            "No shared key for " + providerKey));
            apiKey = encryptionService.decrypt(sk.getKeyValue());
            builder.setUseSharedKeys(true);
            builder.setSharedKeyId(sk.getId());
        } else {
            apiKey = userService.getUserKeys(user).get(providerKey);
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("Missing API key for " + providerKey);
            }
        }
        builder.setKey(apiKey);
    }

    private void applyTools(ChatSession session, RequestBuilder builder) {
        List<String> enabled = new ArrayList<>(session.getEnabledTools());
        if (enabled.isEmpty())
            return;
        List<ToolDefinition> defs = toolRegistry.resolve(enabled);
        if (!defs.isEmpty()) {
            builder.setTools(defs.stream().map(ToolDefinition::toOpenAiFormat).toList());
        }
    }
}
