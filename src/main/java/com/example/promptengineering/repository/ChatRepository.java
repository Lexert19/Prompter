package com.example.promptengineering.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.promptengineering.entity.Chat;

public interface ChatRepository extends ReactiveMongoRepository<Chat, String> {
}