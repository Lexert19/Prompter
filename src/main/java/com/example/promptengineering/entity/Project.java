package com.example.promptengineering.entity;

import java.util.List;


import com.example.promptengineering.model.FileElement;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection = "projects") 
public class Project {
    @Id
    private String id;
    private String name;
    private String userId;
    @DocumentReference  
    private User user;
    @DocumentReference 
    private List<FileElement> files;


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }


    
    
}
