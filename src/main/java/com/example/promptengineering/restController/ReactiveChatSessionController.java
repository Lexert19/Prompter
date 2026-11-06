package com.example.promptengineering.restController;

import com.example.promptengineering.dto.SessionChatRequest;
import com.example.promptengineering.dto.SessionMessageDto;
import com.example.promptengineering.dto.SessionStateResponse;
import com.example.promptengineering.dto.ToolCallDto;
import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat/session")
@RequiredArgsConstructor
@Slf4j
public class ReactiveChatSessionController {

    private final ChatSessionService sessionService;
    private final ChatSessionRegistry registry;
    private final ChatSessionPersistenceService chatSessionPersistenceService;

    @PostMapping(value = "/{chatUuid}/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@AuthenticationPrincipal User user,
                                              @PathVariable UUID chatUuid,
                                              @RequestBody SessionChatRequest req) {
        return sessionService.process(user, chatUuid, req);
    }

    @GetMapping("/{chatUuid}/state")
    public ResponseEntity<SessionStateResponse> state(@AuthenticationPrincipal User user,
                                                      @PathVariable UUID chatUuid) {
        return registry.get(chatUuid, user.getId()).map(this::toState)
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{chatUuid}")
    public ResponseEntity<Void> reset(@AuthenticationPrincipal User user,
                                      @PathVariable UUID chatUuid) {
        registry.remove(chatUuid, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{chatUuid}/messages")
    public ResponseEntity<List<SessionMessageDto>> messages(@AuthenticationPrincipal User user,
                                                            @PathVariable UUID chatUuid) {
        Optional<ChatSession> inMemory = registry.get(chatUuid, user.getId());

        if (inMemory.isPresent() && inMemory.get().isHydrated()
                && !inMemory.get().getMessages().isEmpty()) {
            return ResponseEntity.ok(toDtoList(inMemory.get()));
        }

        try {
            Chat chat = chatSessionPersistenceService.ensureChat(chatUuid, user);
            List<SessionMessage> loaded = chatSessionPersistenceService
                    .loadMessages(chat);

            ChatSession s = registry.getOrCreate(chatUuid, user.getId());
            s.setChatEntity(chat);
            s.getMessages().clear();
            s.getMessages().addAll(loaded);
            s.setHydrated(true);

            return ResponseEntity.ok(loaded.stream().map(this::toDto).toList());
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            log.error("Failed to load messages for chat {}", chatUuid, e);
            return ResponseEntity.ok(List.of());
        }
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

    private List<SessionMessageDto> toDtoList(ChatSession s) {
        return s.getMessages().stream().map(this::toDto).toList();
    }

    private SessionStateResponse toState(ChatSession s) {
        return new SessionStateResponse(s.getChatUuid(), s.getModel(), s.getProvider(),
                s.getSystemPrompt(), new ArrayList<>(s.getEnabledTools()),
                s.getMessages().size(),
                s.getMessages().stream()
                        .anyMatch(m -> m.getProviderMessage().getToolCalls() != null
                                && !m.getProviderMessage().getToolCalls().isEmpty()));
    }
}
