package com.example.promptengineering.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;

import reactor.core.publisher.Mono;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public Mono<User> setClaudeKey(String key, User user){
        user.setClaudeKey(key);
        return userRepository.save(user);
    }

    public Mono<User> setChatgptKey(String key, User user) {
        user.setChatgptKey(key);
        return userRepository.save(user);
    }

    public Mono<User> setNvidiaKey(String key, User user) {
        user.setNvidiaKey(key);
        return userRepository.save(user);
    }
}
