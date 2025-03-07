package com.example.promptengineering.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.promptengineering.entity.FileElement;

import reactor.core.publisher.Flux;

public interface FileElementsRepository extends ReactiveMongoRepository<FileElement, String> {
    Flux<FileElement> findByProject(String projectId);
}

