package com.example.promptengineering.controller;

import com.example.promptengineering.dto.PostDto;
import com.example.promptengineering.exception.ResourceNotFoundException;
import com.example.promptengineering.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/blog")
public class BlogApiController {

    private final PostService postService;

    public BlogApiController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostDto>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long id) throws ResourceNotFoundException {
        PostDto post = postService.getPostDtoById(id);
        return ResponseEntity.ok(post);
    }

    @PostMapping("/posts")
    public ResponseEntity<PostDto> createPost(@RequestBody PostDto postDto) throws ResourceNotFoundException {
        PostDto saved = postService.createPost(postDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/posts/{id}")
    public ResponseEntity<PostDto> updatePost(@PathVariable Long id, @RequestBody PostDto postDto) throws ResourceNotFoundException {
        PostDto updated = postService.updatePost(id, postDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) throws ResourceNotFoundException {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}