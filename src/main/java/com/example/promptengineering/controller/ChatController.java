package com.example.promptengineering.controller;

import com.example.promptengineering.service.JsBundleService;
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
    private final JsBundleService bundleService;

    public ChatController(JsBundleService bundleService) {
        this.bundleService = bundleService;
    }

    @GetMapping(value = {"/chat",
            "/chat/{chatId}"}, produces = "text/html; charset=UTF-8")
    public ModelAndView getChatPage() {
        ModelAndView modelAndView = new ModelAndView("chat");
        modelAndView.addObject("variable", "value");
        modelAndView.addObject("jsBundleUrl", bundleService.url());
        return modelAndView;
    }

}
