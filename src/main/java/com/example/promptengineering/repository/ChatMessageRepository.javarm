package com.example.promptengineering.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

import com.example.promptengineering.entity.ChatMessage;

import reactor.core.publisher.Flux;

@Repository
public interface ChatMessageRepository  extends ReactiveCassandraRepository<ChatMessage, Long>{
    //Flux<ChatMessage> findAllByChatHistoryIdAndUserId(String chatHistoryId, String userId);
}
