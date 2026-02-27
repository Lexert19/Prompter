package com.example.promptengineering.repository;

import com.example.promptengineering.entity.MigrationFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MigrationFlagRepository extends JpaRepository<MigrationFlag, Long> {
    Optional<MigrationFlag> findByName(String name);
}