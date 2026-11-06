package com.example.promptengineering.service;

import java.util.Map;

public interface ToolExecutor {
    String name();
    String execute(Map<String, Object> arguments) throws Exception;
}
