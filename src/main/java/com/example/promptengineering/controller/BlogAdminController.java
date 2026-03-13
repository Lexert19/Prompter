package com.example.promptengineering.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/blog")
public class BlogAdminController {
    @GetMapping
    public String list(Model model) {
        return "admin/blog/list";
    }

    @GetMapping("/new")
    public String newPost() {
        return "admin/blog/edit";
    }

    @GetMapping("/edit/{id}")
    public String editPost(@PathVariable Long id) {
        return "admin/blog/edit";
    }
}
