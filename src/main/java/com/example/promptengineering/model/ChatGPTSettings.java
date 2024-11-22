package com.example.promptengineering.model;

import lombok.Data;

@Data
public class ChatGPTSettings {
    private String apiKey;
   
    public ChatGPTSettings(String apiKey){
        this.apiKey = apiKey;
    }
}
