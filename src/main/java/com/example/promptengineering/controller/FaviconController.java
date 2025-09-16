package com.example.promptengineering.controller;

import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class FaviconController {
    @GetMapping("/favicon")
    public ResponseEntity<byte[]> getFavicon() throws IOException {
        Resource resource = new ClassPathResource("static/favicon.ico");
        byte[] fileContent = FileCopyUtils.copyToByteArray(resource.getInputStream());
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(fileContent);
    }


}
