package com.example.promptengineering.service;

import com.example.promptengineering.model.ToolDefinition;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DateTimeTool implements ToolExecutor {

    private final ToolRegistry registry;

    @Override
    public String name() {
        return "get_current_datetime";
    }

    @PostConstruct
    void register() {
        registry.register(new ToolDefinition(name(),
                "Returns current date and time (ISO-8601). Optional IANA timezone.",
                Map.of("type", "object", "properties",
                        Map.of("timezone",
                                Map.of("type", "string", "description",
                                        "e.g. Europe/Warsaw, America/New_York")),
                        "required", List.of())),
                this);
    }

    @Override
    public String execute(Map<String, Object> args) {
        String tz = String.valueOf(args.getOrDefault("timezone", "UTC"));
        return ZonedDateTime.now(ZoneId.of(tz))
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
