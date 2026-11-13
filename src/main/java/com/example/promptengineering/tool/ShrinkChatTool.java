package com.example.promptengineering.tool;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.ToolDefinition;
import com.example.promptengineering.service.ChatCompactionService;
import com.example.promptengineering.service.ChatSession;
import com.example.promptengineering.service.ToolExecutor;
import com.example.promptengineering.service.ToolRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShrinkChatTool implements ToolExecutor {

    private final ToolRegistry registry;
    private final ChatCompactionService compaction;
    private final ObjectMapper mapper;

    @Override
    public String name() {
        return "shrink_chat";
    }

    @PostConstruct
    void register() {
        registry.register(new ToolDefinition(name(),
                "Exports the most important rounds of this chat into a downloadable .txt file. "
                        + "Does NOT modify the chat. Use when the conversation gets long and the user "
                        + "wants a condensed version to keep.",
                Map.of("type", "object", "properties",
                        Map.of("keep_rounds", Map.of("type", "integer", "default", 25,
                                "minimum", 5, "maximum", 100)),
                        "required", List.of())),
                this);
    }

    @Override
    public String execute(Map<String, Object> args) {
        throw new IllegalStateException("shrink_chat requires session context");
    }

    @Override
    public String execute(Map<String, Object> args, User user, ChatSession session)
            throws Exception {
        int keep = ((Number) args.getOrDefault("keep_rounds", 25)).intValue();

        ChatCompactionService.ExportResult r = compaction.export(user, session,
                Math.max(2, keep));

        return mapper.writeValueAsString(
                Map.of("fileId", r.fileId(), "fileName", r.fileName(), "sizeBytes",
                        r.sizeBytes(), "keptRounds", r.keptRounds(), "removedRounds",
                        r.removedRounds(), "downloadUrl", "/api/files/" + r.fileId()));
    }
}
