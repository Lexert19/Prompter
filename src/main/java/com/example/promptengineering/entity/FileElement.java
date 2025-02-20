package com.example.promptengineering.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "fileElements") 
public class FileElement {
    @Id
    private String id;
    private String name;
    private String project;
    private String userId;
    private String content;
    private List<String> pages;
    private List<List<Double>> vectors;

    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public List<String> getPages() {
        return pages;
    }
    public void setPages(List<String> pages) {
        this.pages = pages;
    }
    public List<List<Double>> getVectors() {
        return vectors;
    }
    public void setVectors(List<List<Double>> vectors) {
        this.vectors = vectors;
    }
    public String getProject() {
        return project;
    }
    public void setProject(String project) {
        this.project = project;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
   
    

    

    
}
