package com.example.promptengineering.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.example.promptengineering.entity.ResetToken;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ResetTokenRepository extends ReactiveMongoRepository<ResetToken, String>{

    
    Flux<ResetToken> findByUserLogin(String userLogin);
    Mono<ResetToken> findByToken(String token);
    Mono<Void> deleteByToken(String token);
    Mono<Void> deleteByUserLogin(String userLogin);
}
