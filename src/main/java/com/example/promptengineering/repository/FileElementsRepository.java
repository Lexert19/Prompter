package com.example.promptengineering.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.promptengineering.entity.FileElement;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

public interface FileElementsRepository extends MongoRepository<FileElement, String> {
    List<FileElement> findByProject(String project);
    Optional<FileElement> findByIdAndProject(String id, String projectId);
}

