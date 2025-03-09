package com.example.promptengineering.entity;

import java.time.LocalDate;

public class Message {
    private String id;
    private String chatId;
    private String message;
    private LocalDate createdAt;
    private int duration;


    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getChatId() {
        return chatId;
    }
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public LocalDate getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
    public int getDuration() {
        return duration;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }


}
