package com.example.promptengineering.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Id;

public class Message {
    @Id
    private String id;
    private String chatId;
    private String text;
    private List<String> documents;
    private List<String> images;  
    private LocalDate createdAt;
    private int duration;
    private String role;
    private String cache;


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
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public List<String> getDocuments() {
        return documents;
    }
    public void setDocuments(List<String> documents) {
        this.documents = documents;
    }
    public List<String> getImages() {
        return images;
    }
    public void setImages(List<String> images) {
        this.images = images;
    }

    


}
