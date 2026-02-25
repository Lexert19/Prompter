package com.example.promptengineering.controller;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Controller
public class ChatController {

    @GetMapping(value = {"/chat", "/chat/{chatId}"}, produces = "text/html; charset=UTF-8")
    public ModelAndView  getChatPage() {
        ModelAndView modelAndView = new ModelAndView("chat");
        modelAndView.addObject("zmienna", "wartosc");
        List<String> jsFiles = getJsFiles();
        modelAndView.addObject("jsFiles", jsFiles);
        return modelAndView;
    }

    public List<String> getJsFiles() {
        List<String> jsFiles = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources("classpath:static/js/*.js");
            for (Resource resource : resources) {
                jsFiles.add("/static/js/" + resource.getFilename());
            }
        } catch (IOException e) {
            Logger log = LoggerFactory.getLogger(getClass());
            log.error("Failed to load JS files from classpath", e);
        }
        return jsFiles;
    }
    
}
