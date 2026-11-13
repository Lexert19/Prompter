package com.example.promptengineering.service;

import com.example.promptengineering.entity.User;

import java.util.Map;

public interface ToolExecutor {
    String name();
    String execute(Map<String, Object> arguments) throws Exception;
    default String execute(Map<String, Object> arguments, User user) throws Exception {
        return execute(arguments);
    }
    default String execute(Map<String, Object> arguments, User user, ChatSession session)
            throws Exception {
        return execute(arguments, user);
    }
}
