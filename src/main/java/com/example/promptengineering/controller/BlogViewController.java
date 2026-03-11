package com.example.promptengineering.controller;

import com.example.promptengineering.dto.PostDto;
import com.example.promptengineering.entity.Post;
import com.example.promptengineering.exception.ResourceNotFoundException;
import com.example.promptengineering.service.PostService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BlogViewController {
    private final PostService postService;

    public BlogViewController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/blog")
    public String list(Model model) {
        model.addAttribute("posts", postService.getAllPosts());
        return "blog/list";
    }

    @GetMapping("/blog/{slug}")
    public String post(@PathVariable String slug, Model model) throws ResourceNotFoundException {
        PostDto post = postService.getPostDtoBySlug(slug);
        model.addAttribute("post", post);
        return "blog/post";
    }
}
