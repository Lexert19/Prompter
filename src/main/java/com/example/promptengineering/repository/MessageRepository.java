package com.example.promptengineering.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.promptengineering.entity.Message;

import reactor.core.publisher.Flux;

import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findByChatId(String chatId);

    void deleteByChatId(String chatId);
}