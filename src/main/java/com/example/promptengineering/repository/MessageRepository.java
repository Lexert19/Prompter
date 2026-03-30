package com.example.promptengineering.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.promptengineering.entity.Message;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatId(Long chatId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.chat.id = :chatId")
    void deleteByChatId(@Param("chatId") Long chatId);
}
