package com.example.promptengineering.entity;

import java.time.LocalDateTime;

import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import com.example.promptengineering.staticUtils.RandomUtils;

@Table
public class ChatMessage {
    @PrimaryKey
    @CassandraType(type = Name.BIGINT)
    private Long id;
    private String chatHistoryId;
    private boolean cache;
    private String content;
    private String userId;
    @CassandraType(type = Name.TIMESTAMP)
    private LocalDateTime createTime;

     public ChatMessage() {
        this.id = RandomUtils.getLong();
        this.createTime = LocalDateTime.now();
    }

    public boolean isCache() {
        return cache;
    }
    public void setCache(boolean cache) {
        this.cache = cache;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
 
    public String getChatHistoryId() {
        return chatHistoryId;
    }
    public void setChatHistoryId(String chatHistoryId) {
        this.chatHistoryId = chatHistoryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    
    
}
