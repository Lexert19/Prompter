package com.example.promptengineering.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
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

  @Cacheable(value = "locales", key = "#lang")
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

    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();
        if (!trimmed.isEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("!")) {
          int separatorIndex = trimmed.indexOf('=');
          if (separatorIndex == -1) {
            separatorIndex = trimmed.indexOf(':');
          }
          if (separatorIndex != -1) {
            String key = trimmed.substring(0, separatorIndex).trim();
            String value = trimmed.substring(separatorIndex + 1).trim();
            target.put(key, value);
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(" " + location, e);
    }
  }
}