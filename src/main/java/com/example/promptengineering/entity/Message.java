package com.example.promptengineering.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
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
    @Column(columnDefinition = "TEXT")
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

    public boolean getCache() {
        return cache;
    }

}
