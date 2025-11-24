package com.example.promptengineering.repository;

import com.example.promptengineering.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.promptengineering.entity.Project;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndUser(Long id, User user);
    List<Project> findAllByUser(User user);
}