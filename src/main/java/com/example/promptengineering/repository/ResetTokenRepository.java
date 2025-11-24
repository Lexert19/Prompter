package com.example.promptengineering.repository;

import com.example.promptengineering.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.promptengineering.entity.ResetToken;

import java.util.List;
import java.util.Optional;

public interface ResetTokenRepository extends JpaRepository<ResetToken, Long> {


    List<ResetToken> findByUser(User user);
    Optional<ResetToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteByUser(User user);

    List<ResetToken> findByUserLogin(String email);

    void deleteByUserLogin(String mail);
}
