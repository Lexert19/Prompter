package com.example.promptengineering.config;

import com.example.promptengineering.model.Content;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfig {
    @Bean
    public Gson gson() {
        return new GsonBuilder().setStrictness(Strictness.LENIENT).serializeNulls()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .registerTypeAdapter(Content.class, new ContentDeserializer()).create();
    }
}
