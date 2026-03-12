package com.example.promptengineering.model;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScoredFragment {
    private String text;
    private double score;

    public ScoredFragment(String text, double score) {
        this.text = text;
        this.score = score;
    }

}