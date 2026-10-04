package com.example.promptengineering.restController;

import com.example.promptengineering.service.LocaleService;
import java.io.IOException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class LocaleController {

    private final LocaleService localeService;

    public LocaleController(LocaleService localeService) {
        this.localeService = localeService;
    }

    @GetMapping(value = "/static/locales/{lang}.json", produces = "application/json")
    public ResponseEntity<Map<String, String>> getLocales(@PathVariable String lang)
            throws IOException {
        return ResponseEntity.ok(localeService.getTranslations(lang));
    }
}
