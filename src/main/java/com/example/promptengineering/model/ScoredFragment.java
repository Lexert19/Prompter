package com.example.promptengineering.model;


public class ScoredFragment {
    private String text;
    private double score;

    public ScoredFragment(String text, double score) {
        this.text = text;
        this.score = score;
    }

    public String getText() {
        return text;
    }
    public double getScore() { 
        return score;
    }

}