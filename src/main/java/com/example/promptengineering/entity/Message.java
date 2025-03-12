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
    private long start;
    private long end;
    private String role;
    private boolean cache;


    public String getRole(){
        return role;
    }
    public void setRole(String role){
        this.role = role;
    }
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


    public boolean getCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }
    public long getStart() {
        return start;
    }
    public void setStart(long start) {
        this.start = start;
    }
    public long getEnd() {
        return end;
    }
    public void setEnd(long end) {
        this.end = end;
    }


}
