package com.example.promptengineering.dto;

import com.example.promptengineering.entity.Model;

import java.util.List;
import java.util.stream.Collectors;

public class ModelDto {
    private Long id;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static ModelDto toDto(Model model) {
        if (model == null) {
            return null;
        }
        ModelDto dto = new ModelDto();
        dto.setId(model.getId());
        dto.setName(model.getName());
        dto.setText(model.getText());
        dto.setProvider(model.getProvider());
        dto.setUrl(model.getUrl());
        dto.setType(model.getType());
        return dto;
    }

    public static List<ModelDto> toDtoList(List<Model> models) {
        return models.stream()
                .map(ModelDto::toDto)
                .collect(Collectors.toList());
    }
}
