package com.example.promptengineering.repository;

import org.springframework.stereotype.Repository;

import com.example.promptengineering.entity.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;  
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String>{
    Mono<User> findByEmail(String email);

}
