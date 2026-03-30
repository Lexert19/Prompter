package com.example.promptengineering.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Embedding {
    private List<Double> vector;
    private String name;

}
