package com.example.promptengineering.repository;

import org.springframework.stereotype.Repository;

import com.example.promptengineering.entity.Project;
import com.example.promptengineering.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProjectRepository extends ReactiveMongoRepository<Project, String> {

    Mono<Project> findByIdAndUser(String projectId, User user);
    Flux<Project> findAllByUser(User user);
}
