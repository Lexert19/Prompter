package com.example.promptengineering.repository;

import com.example.promptengineering.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.promptengineering.entity.Chat;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    @EntityGraph(attributePaths = {"user"})
    Page<Chat> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    @EntityGraph(attributePaths = {"user"})
    List<Chat> findByUser(User user);
    @EntityGraph(attributePaths = {"user"})
    Optional<Chat> findByUuid(UUID uuid);
}
