package com.example.promptengineering.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;


@Controller
public class ChatController {

    @GetMapping(value = "/chat", produces = "text/html; charset=UTF-8")
    public ModelAndView  getChatPage() {
        ModelAndView modelAndView = new ModelAndView("chat");
        modelAndView.addObject("zmienna", "wartosc");
        return modelAndView;
    }
    
}
