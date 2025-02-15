package com.example.promptengineering.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

import com.example.promptengineering.entity.Project;

import reactor.core.publisher.Mono;

@Repository
public interface ProjectRepository extends ReactiveCassandraRepository<Project, String> {

    Mono<Project> findByIdAndUserId(String projectId, String userId);

}
