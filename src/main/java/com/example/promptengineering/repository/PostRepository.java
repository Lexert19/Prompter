package com.example.promptengineering.repository;

import com.example.promptengineering.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findBySlug(String slug);
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByParentId(Long parentId);
}