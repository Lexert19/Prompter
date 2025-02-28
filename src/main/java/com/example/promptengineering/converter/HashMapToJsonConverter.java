package com.example.promptengineering.converter;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.HashMap;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


@Converter
public class HashMapToJsonConverter implements AttributeConverter<HashMap<String, String>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(HashMap<String, String> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting HashMap to JSON", e);
        }
    }

    @Override
    public HashMap<String, String> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(dbData, new TypeReference<HashMap<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error converting JSON to HashMap", e);
        }
    }
}
