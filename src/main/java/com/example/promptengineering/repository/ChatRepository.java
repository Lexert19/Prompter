package com.example.promptengineering.repository;

import com.example.promptengineering.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.promptengineering.entity.Chat;


import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByUser(User user);
}