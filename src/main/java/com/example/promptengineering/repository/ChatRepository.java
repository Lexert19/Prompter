package com.example.promptengineering.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.promptengineering.entity.Chat;

import reactor.core.publisher.Flux;

public interface ChatRepository extends ReactiveMongoRepository<Chat, String> {
    Flux<Chat> findByUserId(String userId);
}