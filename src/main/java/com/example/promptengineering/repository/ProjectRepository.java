package com.example.promptengineering.repository;

import org.springframework.stereotype.Repository;

import com.example.promptengineering.entity.Project;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProjectRepository extends ReactiveMongoRepository<Project, String> {

    Mono<Project> findByIdAndUserId(String projectId, String userId);
    Flux<Project> findAllByUserId(String userId);
}
