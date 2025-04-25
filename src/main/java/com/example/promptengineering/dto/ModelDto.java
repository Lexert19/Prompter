package com.example.promptengineering.dto;

public class ModelDto {
    private String name;
    private String text;
    private String provider;
    private String url;
    private String type;
    public ModelDto() {}
    public ModelDto(String name, String text, String provider, String url, String type) {
        this.name = name;
        this.text = text;
        this.provider = provider;
        this.url = url;
        this.type = type;
    }
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
