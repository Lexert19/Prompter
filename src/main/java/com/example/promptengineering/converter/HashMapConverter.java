package com.example.promptengineering.converter;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter(autoApply = false) 
public class HashMapConverter implements AttributeConverter<HashMap<String, String>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(HashMap<String, String> attribute) {
        if (attribute == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(attribute); 
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Błąd konwersji HashMap na JSON", e);
        }
    }

    @Override
    public HashMap<String, String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new HashMap<>(); 
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<HashMap<String, String>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Błąd konwersji JSON na HashMap", e);
        }
    }
}