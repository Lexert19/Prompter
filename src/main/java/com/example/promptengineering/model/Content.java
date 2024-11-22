package com.example.promptengineering.model;

import java.util.HashMap;
import java.util.Map;

public class Content {
    private String type;
    private String text;
    private String mediaType;
    private String data;

    public String getText() { return text; }
    public String getMediaType() { return mediaType; }
    public String getData() { return data; }
    

    public Map<String, Object> toMap(String provider, boolean cached) {
        switch(type) {
            case "text" -> {
                return handleText(provider, cached);
            }
            case "image" -> {
                return handleImage(provider, cached);
            }
            default -> throw new IllegalStateException("Unknown content type");
        }
    }

    private Map<String, Object> handleText(String provider, boolean cache) {
        Map<String, Object> map = new HashMap<>();
        if (!"GEMINI".equals(provider)) {
            map.put("type", "text");
        }
        map.put("text", text);

        if (cache && provider.equals("ANTHROPIC")) {
            map.put("cache_control", new CacheControl().toMap());
        }
        return map;
    }

    private Map<String, Object> handleImage(String provider, boolean cache) {
        if (provider.equals("ANTHROPIC")) {
            Map<String, Object> map = new HashMap<>();
            map.put("type", "image");
            map.put("source", Map.of(
                "type", "base64",
                "media_type", mediaType,
                "data", data
            ));
            
            if (cache) {
                map.put("cache_control", new CacheControl().toMap());
            }
            return map;
        } 
        if (provider.equals("OPENAI")) {
            return Map.of(
                "type", "image_url",
                "image_url", Map.of(
                    "url", "data:" + mediaType + ";base64," + data
                )
            );
        }
        if ("GEMINI".equals(provider)) {
            return Map.of(
                "inline_data", Map.of(
                    "mime_type", mediaType,
                    "data", data
                )
            );
        }
        // if(provider.equals("GEMINI")){
        //     return Map.of(
        //         "text", text
        //     );
        // }
       
        throw new UnsupportedOperationException("Base64 images not supported for " + provider);
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }
    public void setData(String data) {
        this.data = data;
    }

    
}