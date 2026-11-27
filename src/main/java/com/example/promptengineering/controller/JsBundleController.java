package com.example.promptengineering.controller;

import com.example.promptengineering.service.JsBundleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;

@Controller
@RequiredArgsConstructor
public class JsBundleController {

    private final JsBundleService bundleService;

    @GetMapping("/js/bundle-{version}.js")
    public ResponseEntity<byte[]> bundle(@PathVariable String version) {
        if (!version.equals(bundleService.getVersion())) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "javascript", java.nio.charset.StandardCharsets.UTF_8))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable())
                .eTag("\"" + version + "\"")
                .body(bundleService.getBundle());
    }
}