package com.example.promptengineering.model;

import java.util.List;

public class Embedding {
    private List<Double> vector;
    private String name;
    
    public List<Double> getVector() {
        return vector;
    }
    public void setVector(List<Double> vector) {
        this.vector = vector;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    
}
