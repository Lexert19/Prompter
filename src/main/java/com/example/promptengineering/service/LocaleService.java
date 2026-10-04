package com.example.promptengineering.service;

import java.io.InputStream;
import java.util.Properties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LocaleService {

    private final ResourceLoader resourceLoader;

    public LocaleService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Cacheable(value = "locales", key = "#lang == null ? 'en' : #lang.toLowerCase()")
    public Map<String, String> getTranslations(String lang) {
        Map<String, String> translations = new LinkedHashMap<>();

        loadProperties("classpath:messages.properties", translations);

        if (lang != null && !lang.isBlank() && !"en".equalsIgnoreCase(lang)) {
            loadProperties("classpath:messages_" + lang + ".properties", translations);
        }

        return translations;
    }

    private void loadProperties(String location, Map<String, String> target) {
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            return;
        }
        Properties props = new Properties();
        try (InputStream is = resource.getInputStream()) {
            props.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + location, e);
        }
        props.forEach((k, v) -> target.put(k.toString(), v.toString()));
    }
}
