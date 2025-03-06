package com.example.promptengineering.model;

import java.util.List;

public class FileElement {
    private String name;
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
   

    

    
}
