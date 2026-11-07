package com.example.promptengineering.service;

import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.Message;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.Content;
import com.example.promptengineering.model.TextContent;
import com.example.promptengineering.model.ToolCall;
import com.example.promptengineering.repository.ChatRepository;
import com.example.promptengineering.repository.MessageRepository;
import com.example.promptengineering.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatSessionPersistenceService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Transactional
    public Chat ensureChat(UUID chatUuid, User user) {
        Chat chat = chatRepository.findByUuid(chatUuid).orElse(null);
        if (chat == null) {
            chat = new Chat();
            chat.setUuid(chatUuid);
            chat.setUser(user);
            chat.setCreatedAt(Instant.now());
            chat = chatRepository.save(chat);
        } else if (!chat.getUser().getId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Session belongs to another user");
        }
        return chat;
    }

    @Transactional(readOnly = true)
    public List<SessionMessage> loadMessages(Chat chat) {
        return messageRepository.findByChatId(chat.getId()).stream()
                .map(this::toSessionMessage).toList();
    }

    @Transactional
    public void save(Chat chat, SessionMessage sm) {
        Chat managedChat = chatRepository.findById(chat.getId()).orElseThrow(
                () -> new IllegalArgumentException("Chat not found: " + chat.getId()));

        Message m = new Message();
        m.setChat(managedChat);
        m.setUuid(sm.getUuid());
        m.setText(sm.getText());
        m.setDocuments(sm.getDocuments());
        m.setImages(sm.getImages());
        m.setRole(sm.getRole());
        m.setCache(sm.isCached());
        m.setStart(sm.getStart());
        m.setEnd(sm.getEnd());
        m.setCreatedAt(sm.getCreatedAt());
        m.setReasoning(sm.getReasoning());

        com.example.promptengineering.model.Message pm = sm.getProviderMessage();
        if (pm.getToolCalls() != null && !pm.getToolCalls().isEmpty()) {
            try {
                m.setToolCallsJson(objectMapper.writeValueAsString(pm.getToolCalls()));
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize toolCalls for message {}", sm.getUuid(), e);
            }
        }
        m.setToolCallId(pm.getToolCallId());
        m.setToolName(pm.getToolName());
        m.setStringContent(pm.getStringContent());

        messageRepository.saveAndFlush(m);
    }

    private SessionMessage toSessionMessage(Message m) {
        List<Content> content = new ArrayList<>();
        if (m.getText() != null && !m.getText().isEmpty()) {
            TextContent tc = new TextContent();
            tc.setType("text");
            tc.setText(m.getText());
            content.add(tc);
        }
        com.example.promptengineering.model.Message pm = new com.example.promptengineering.model.Message();
        pm.setRole(m.getRole());
        pm.setContent(content);
        pm.setCached(m.getCache());
        pm.setToolCallId(m.getToolCallId());
        pm.setToolName(m.getToolName());
        pm.setStringContent(m.getStringContent());

        if (m.getToolCallsJson() != null && !m.getToolCallsJson().isBlank()) {
            try {
                pm.setToolCalls(objectMapper.readValue(m.getToolCallsJson(),
                        new TypeReference<List<ToolCall>>() {
                        }));
            } catch (JsonProcessingException e) {
                log.warn("Failed to deserialize toolCalls for message {}", m.getUuid(),
                        e);
            }
        }

        return new SessionMessage(pm, m.getDocuments(), m.getImages(), m.getStart(),
                m.getEnd(), m.getUuid(), m.getCreatedAt(), m.getReasoning());
    }

    @Transactional(readOnly = true)
    public List<Message> findUserPrompts(UUID chatUuid, Long userId) {
        return messageRepository.findUserPrompts(chatUuid, userId);
    }

    @Transactional(readOnly = true)
    public boolean chatExistsForUser(UUID chatUuid, Long userId) {
        return chatRepository.existsByUuidAndUserId(chatUuid, userId);
    }

    @Transactional
    public List<SessionMessage> getOrLoad(UUID chatUuid, User user,
                                          ChatSessionRegistry registry) {
        var inMemory = registry.get(chatUuid, user.getId());
        if (inMemory.isPresent()) {
            return inMemory.get().getMessages();
        }
        Chat chat = ensureChat(chatUuid, user);
        List<SessionMessage> loaded = loadMessages(chat);
        ChatSession s = registry.getOrCreate(chatUuid, user.getId());
        s.setChatEntity(chat);
        s.getMessages().addAll(loaded);
        s.setHydrated(true);
        return loaded;
    }
}
