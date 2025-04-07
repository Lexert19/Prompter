package com.example.promptengineering.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.promptengineering.entity.Chat;

import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatRepository extends MongoRepository<Chat, String> {
    List<Chat> findByUserId(String userId);
}