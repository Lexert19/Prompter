package com.example.promptengineering.converter;

import java.util.HashMap;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@WritingConverter
public class HashMapToJsonConverter implements Converter<HashMap<String, String>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convert(HashMap<String, String> source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
