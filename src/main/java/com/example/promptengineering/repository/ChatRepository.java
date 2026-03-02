package com.example.promptengineering.repository;

import com.example.promptengineering.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.promptengineering.entity.Chat;


import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Page<Chat> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    List<Chat> findByUser(User user);
}