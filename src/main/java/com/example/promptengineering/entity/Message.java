package com.example.promptengineering.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Id;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @Column(columnDefinition = "TEXT")
    private String text;

    @ElementCollection
    @CollectionTable(name = "message_documents", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "document_id")
    private List<String> documents;

    @ElementCollection
    @CollectionTable(name = "message_images", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "image_url")
    private List<String> images;

    @Column(name = "created_at")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Instant createdAt;

    private long start;

    @Column(name = "end_time")
    private long end;

    private String role;

    private boolean cache;


    public String getRole(){
        return role;
    }
    public void setRole(String role){
        this.role = role;
    }


    public boolean isCache() {
        return cache;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }
}
