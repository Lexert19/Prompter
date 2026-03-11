package com.example.promptengineering.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/blog")
public class BlogAdminController {

    public BlogAdminController() {
    }

    @GetMapping
    public String list(Model model) {
        return "admin/blog/list";
    }

}
