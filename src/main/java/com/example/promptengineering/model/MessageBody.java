package com.example.promptengineering.model;

import java.util.List;

public class MessageBody {
    private String chatId;
    private String text;
    private List<String> documents;
    private List<String> images;
    private int duration;
    private String role;
    private String cache;


    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
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


    public boolean isCache() {
        return cache;
    }

    public void setCache(boolean cache) {
        this.cache = cache;
    }

    public String getRole(){
        return role;
    }
    public void setRole(String role){
        this.role = role;
    }

}
