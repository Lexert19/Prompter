package com.example.promptengineering.repository;

import org.springframework.stereotype.Repository;

import com.example.promptengineering.entity.Project;
import com.example.promptengineering.entity.User;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.repository.query.Param;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProjectRepository extends ReactiveMongoRepository<Project, String> {

    Mono<Project> findByIdAndUser(String projectId, User user);
    @Query("{ 'user.$id': ?#{#user.id} }")
    Flux<Project> findAllByUser(@Param("user") User user);
}
