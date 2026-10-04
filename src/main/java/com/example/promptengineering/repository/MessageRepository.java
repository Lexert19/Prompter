package com.example.promptengineering.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.promptengineering.entity.Message;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByChatId(Long chatId);
    long countByChatId(Long chatId);

    @Query("""
                SELECT COALESCE(SUM(LENGTH(m.text)), 0)
                FROM Message m
                WHERE m.chat.id = :chatId
            """)
    long sumTextLengthByChatId(@Param("chatId") Long chatId);

    @Query("""
                SELECT COALESCE(SUM(LENGTH(d)), 0)
                FROM Message m JOIN m.documents d
                WHERE m.chat.id = :chatId
            """)
    long sumDocumentLengthByChatId(@Param("chatId") Long chatId);

    @Query("""
                SELECT COALESCE(SUM(LENGTH(i)), 0)
                FROM Message m JOIN m.images i
                WHERE m.chat.id = :chatId
            """)
    long sumImageLengthByChatId(@Param("chatId") Long chatId);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.chat.id = :chatId")
    void deleteByChatId(@Param("chatId") Long chatId);
}
