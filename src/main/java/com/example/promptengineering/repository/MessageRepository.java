package com.example.promptengineering.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.promptengineering.entity.Message;

import reactor.core.publisher.Flux;

public interface MessageRepository extends ReactiveMongoRepository<Message, String> {

    Flux<Message> findByChatId(String chatId); 
}