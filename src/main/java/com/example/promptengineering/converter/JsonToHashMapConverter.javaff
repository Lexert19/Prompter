package com.example.promptengineering.converter;

import java.util.HashMap;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ReadingConverter
public class JsonToHashMapConverter implements Converter<String, HashMap<String, String>> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public HashMap<String, String> convert(String source) {
        try {
            return objectMapper.readValue(source, 
                new TypeReference<HashMap<String, String>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}