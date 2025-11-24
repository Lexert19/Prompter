package com.example.promptengineering.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.promptengineering.entity.Message;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatId(Long chatId);

    void deleteByChatId(Long chatId);
}