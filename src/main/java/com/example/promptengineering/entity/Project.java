package com.example.promptengineering.entity;

import java.util.List;


import com.example.promptengineering.model.Embedding;
import com.example.promptengineering.model.FileElement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "projects") 
public class Project {
    @Id
    private String id;
    private String name;
    @DBRef 
    private User user;
    private List<FileElement> files;
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

  

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }


    
}
