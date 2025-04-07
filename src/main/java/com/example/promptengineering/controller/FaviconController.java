package com.example.promptengineering.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

public class FaviconController {
    @GetMapping("/favicon.ico")
    public ResponseEntity<Resource> getFavicon() {
        Resource resource = new ClassPathResource("static/favicon.ico");
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/x-icon"))
                .body(resource);
    }
}
