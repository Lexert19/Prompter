package com.example.promptengineering.restController;

import com.example.promptengineering.model.ToolDefinition;
import com.example.promptengineering.service.ToolRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tools")
@RequiredArgsConstructor
public class ToolController {
    private final ToolRegistry toolRegistry;

    @GetMapping
    public List<ToolDefinition> getAllTools() {
        return new ArrayList<>(toolRegistry.all());
    }
}
