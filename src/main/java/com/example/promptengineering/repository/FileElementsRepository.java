package com.example.promptengineering.repository;

import com.example.promptengineering.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.promptengineering.entity.FileElement;

import java.util.List;
import java.util.Optional;

public interface FileElementsRepository extends JpaRepository<FileElement, Long> {

    List<FileElement> findByProject(Project project);
    Optional<FileElement> findByIdAndProject(Long id, Project project);
}