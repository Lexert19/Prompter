package com.example.promptengineering.entity;

import jakarta.persistence.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "models")
public class Model {
    @Id
    private String id;
    private String name;
    private String text;
    private String provider;
    private String url;
    private boolean global;
    private String type;
    private String userId;
    public Model() {}
    public Model(String name, String text, String provider, String url, String type, String userId) {
        this.name = name;
        this.text = text;
        this.provider = provider;
        this.url = url;
        this.type = type;
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}