package com.example.promptengineering.dto;

import com.example.promptengineering.entity.Model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ModelDto {
    private Long id;
    private String name;
    private String text;
    private String provider;
    private String url;
    private String type;
    private double pointsPerInput;
    private double pointsPerOutput;

    public ModelDto() {
    }

    public ModelDto(String name, String text, String provider, String url, String type) {
        this.name = name;
        this.text = text;
        this.provider = provider;
        this.url = url;
        this.type = type;
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
        dto.setPointsPerInput(model.getPointsPerInput());
        dto.setPointsPerOutput(model.getPointsPerOutput());
        return dto;
    }

    public static List<ModelDto> toDtoList(List<Model> models) {
        return models.stream().map(ModelDto::toDto).collect(Collectors.toList());
    }
}
