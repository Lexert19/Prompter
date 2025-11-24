package com.example.promptengineering.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;

import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


//    public Mono<User> login(String login, String password) {
//        return userRepository.findByEmail(login)
//                .flatMap(user -> {
//                    if (passwordEncoder.matches(password, user.getPassword())) {
//                        return Mono.just(user);
//                    } else {
//                        return Mono.error(new RuntimeException("Invalid credentials"));
//                    }
//                })
//                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
//    }

    public User updatePassword(User user, String newPassword){
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encryptedPassword);
        return userRepository.save(user);
    }

}
