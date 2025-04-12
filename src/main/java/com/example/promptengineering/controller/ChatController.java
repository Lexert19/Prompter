package com.example.promptengineering.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Controller
public class ChatController {

    @GetMapping(value = "/chat", produces = "text/html; charset=UTF-8")
    public ModelAndView  getChatPage() {
        ModelAndView modelAndView = new ModelAndView("chat");
        modelAndView.addObject("zmienna", "wartosc");
        List<String> jsFiles = getJsFiles();
        modelAndView.addObject("jsFiles", jsFiles);
        return modelAndView;
    }

    public List<String> getJsFiles() {
        List<String> jsFiles = new ArrayList<>();
        try {
            Resource resource = new ClassPathResource("static/js");
            File directory = resource.getFile();
            for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".js")) {
                    jsFiles.add("/static/js/" + file.getName());
                }
            }
        } catch (Exception e) {
        }
        return jsFiles;
    }
    
}
