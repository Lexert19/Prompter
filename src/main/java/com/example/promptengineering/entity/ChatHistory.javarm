package com.example.promptengineering.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.Table;

public class ChatHistory {
    @Id
    private String id;
    private String title;
    private String name;
    private String userId;
    @CassandraType(type = Name.LIST, typeArguments = Name.BIGINT)
    private List<Long> messages;
    @CassandraType(type = Name.TIMESTAMP)
    private LocalDateTime createTime;

    public ChatHistory() {
        this.id = UUID.randomUUID().toString();
        this.createTime = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Long> getMessages() {
        if (messages == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(messages);
    }

    public void setMessages(List<Long> messages) {
        this.messages = messages;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    
}
