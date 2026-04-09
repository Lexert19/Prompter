package com.example.promptengineering.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Controller
public class ChatController {

    @GetMapping(value = {"/chat",
            "/chat/{chatId}"}, produces = "text/html; charset=UTF-8")
    public ModelAndView getChatPage() {
        ModelAndView modelAndView = new ModelAndView("chat");
        modelAndView.addObject("variable", "value");
        List<String> jsFiles = getJsFiles();
        modelAndView.addObject("jsFiles", jsFiles);
        return modelAndView;
    }

    public List<String> getJsFiles() {
        List<String> jsFiles = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:static/js/**/*.js");
            String basePath = "/static/js/";
            for (Resource resource : resources) {
                String uri = resource.getURI().toString();
                int pos = uri.indexOf(basePath);
                if (pos != -1) {
                    String relativePath = uri.substring(pos + basePath.length());
                    jsFiles.add(basePath + relativePath);
                } else {
                    jsFiles.add(basePath + resource.getFilename());
                }
            }
        } catch (IOException e) {
            log.error("Failed to load JS files from classpath", e);
        }
        Collections.sort(jsFiles);
        return jsFiles;
    }

}
