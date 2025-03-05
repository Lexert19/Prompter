package com.example.promptengineering.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import com.example.promptengineering.entity.Project;
import com.example.promptengineering.entity.User;

import reactor.core.publisher.Mono;

@Repository
public interface ProjectRepository extends ReactiveCrudRepository<Project, Long> {

    Mono<Project> findByIdAndUser(String projectId, User user);

}
