package com.example.promptengineering.restController;

import com.example.promptengineering.dto.*;
import com.example.promptengineering.entity.User;
import java.util.UUID;

import com.example.promptengineering.service.ChatSessionPersistenceService;
import com.example.promptengineering.service.ChatSessionRegistry;
import com.example.promptengineering.service.SessionMessage;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.service.HistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    private final HistoryService historyService;
    private final ChatSessionPersistenceService persistence;
    private final ChatSessionRegistry registry;

    public HistoryController(HistoryService historyService,
            ChatSessionPersistenceService persistence, ChatSessionRegistry registry) {
        this.historyService = historyService;
        this.persistence = persistence;
        this.registry = registry;
    }

    @GetMapping("/chats/{chatUuid}/messages")
    public ResponseEntity<List<SessionMessageDto>> getChatHistory(@PathVariable UUID chatUuid,
                                                                  @AuthenticationPrincipal User user) {
        List<SessionMessageDto> messages = persistence.getOrLoad(chatUuid, user, registry)
                .stream().map(this::toDto).toList();
        return ResponseEntity.ok(messages);
    }

    @DeleteMapping("/chats")
    public ResponseEntity<Void> deleteAllChats(@AuthenticationPrincipal User user) {
        historyService.deleteAllChats(user);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/chats/{chatUuid}")
    public ResponseEntity<Void> deleteChat(@PathVariable UUID chatUuid,
                                           @AuthenticationPrincipal User user) {
        historyService.deleteChat(chatUuid, user);
        return ResponseEntity.noContent().build();
    }

    private SessionMessageDto toDto(SessionMessage m) {
        com.example.promptengineering.model.Message pm = m.getProviderMessage();

        List<ToolCallDto> calls = pm.getToolCalls() == null
                ? null
                : pm.getToolCalls().stream().map(tc -> new ToolCallDto(tc.getId(),
                        tc.getName(), tc.getArguments())).toList();

        return new SessionMessageDto(m.getUuid().toString(), m.getRole(), m.getText(),
                m.isCached(), m.getImages(), m.getDocuments(), m.getStart(), m.getEnd(),
                m.getCreatedAt().toEpochMilli(), calls, pm.getToolCallId(),
                pm.getToolName(), pm.getStringContent(), m.getReasoning());
    }

    @GetMapping("/chats")
    public ResponseEntity<PageDto<ChatDto>> getChats(@AuthenticationPrincipal User user,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        Page<Chat> chatPage = historyService.getChatsForUser(user, page, size);
        return ResponseEntity.ok(PageDto.from(chatPage, ChatDto::fromEntity));
    }
}
