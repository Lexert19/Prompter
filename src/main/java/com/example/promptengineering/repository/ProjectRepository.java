package com.example.promptengineering.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.promptengineering.entity.Project;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    Optional<Project> findByIdAndUserId(String projectId, String userId);
    List<Project> findAllByUserId(String userId);
}
