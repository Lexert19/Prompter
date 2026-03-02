package com.example.promptengineering.repository;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.entity.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.Optional;

public interface UserFileRepository extends JpaRepository<UserFile, Long> {
    Optional<UserFile> findByIdAndOwner(Long id, User owner);
    ArrayList<UserFile> findByOwner(User owner);
    long countByOwner(User owner);
}
