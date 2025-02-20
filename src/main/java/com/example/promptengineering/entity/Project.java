package com.example.promptengineering.entity;

import java.util.List;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection = "projects") 
public class Project {
    @Id
    private String id;
    private String name;
    private String userId;


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
