package com.example.promptengineering.entity;

import java.util.List;

import org.springframework.data.cassandra.core.mapping.Table;

import com.example.promptengineering.model.Embedding;
import com.example.promptengineering.model.FileElement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Table("project")
@Entity
public class Project {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;
    private String name;
    @ManyToOne 
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "files", columnDefinition = "jsonb")
    private List<FileElement> files;
    @Column(name = "embeddings", columnDefinition = "jsonb")
    private List<Embedding> embeddings;


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    } 
    public List<Embedding> getEmbeddings() {
        return embeddings;
    }
    public void setEmbeddings(List<Embedding> embeddings) {
        this.embeddings = embeddings;
    }
    public List<FileElement> getFiles() {
        return files;
    }
    public void setFiles(List<FileElement> files) {
        this.files = files;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    
}
