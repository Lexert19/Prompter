package com.example.promptengineering.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.List;

import com.example.promptengineering.model.Content;

@Converter
public class ContentListConverter implements AttributeConverter<List<Content>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<Content> contentList) {
        try {
            return objectMapper.writeValueAsString(contentList);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting list to JSON", e);
        }
    }

    @Override
    public List<Content> convertToEntityAttribute(String contentJson) {
        try {
            return objectMapper.readValue(contentJson, new TypeReference<List<Content>>() {
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting JSON to list", e);
        }
    }
}