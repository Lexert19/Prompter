package com.example.promptengineering.restController;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TranslateController {
    @GetMapping("/public/translate-url")
    public ResponseEntity<String> translateUrl(@RequestParam String url,
                                               @RequestParam String targetLang) {
        String path = url.replaceFirst("^/(pl|en)(/|$)", "/");
        if (!"/".equals(path)) {
            return ResponseEntity.ok(url);
        }
        String translatedUrl = "/" + targetLang + path;
        return ResponseEntity.ok(translatedUrl);
    }
}
