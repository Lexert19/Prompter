package com.example.promptengineering.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;

import reactor.core.publisher.Mono;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public Mono<User> saveKeyToMap(User user, String keyName, String keyValue) {
        user.getKeys().put(keyName, keyValue);
        return userRepository.save(user);
    }

    public Mono<Map<String, String>> getKeys(User user) {
        return Mono.just(user.getKeys());
    }

    public Mono<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
