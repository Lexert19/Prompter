package com.example.promptengineering.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.promptengineering.entity.ResetToken;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface ResetTokenRepository extends MongoRepository<ResetToken, String> {

    
    List<ResetToken> findByUserLogin(String userLogin);
    Optional<ResetToken> findByToken(String token);
    void deleteByToken(String token);
    void deleteByUserLogin(String userLogin);
}
