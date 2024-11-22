package com.example.promptengineering.model;

import lombok.Data;

@Data
public class ClaudeImageContent {
    private String type;
    private ClaudeSource source;

    public ClaudeImageContent(String media_type, String data){
        this.type = "image";
        this.source = new ClaudeSource(media_type, data);
    }

}

@Data
class ClaudeSource{
    private String type;
    private String media_type;
    private String data;

    public ClaudeSource(String media_type, String data){
        this.type = "base64";
        this.media_type = media_type;
        this.data = data;
    }
}