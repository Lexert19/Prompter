package com.example.promptengineering.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nimbusds.oauth2.sdk.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;

import reactor.core.publisher.Mono;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveKeyToMap(User user, String keyName, String keyValue) {
        user.getKeys().put(keyName, keyValue);
        return userRepository.save(user);
    }

    public Map<String, String> getKeys(User user) {
        return user.getKeys();
    }

    public User findUserByEmail(String email) throws Exception {
        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty())
            throw new Exception("User does not exists.");
        return user.get();
    }

    public Object createUser(String email, String password, List<Role> roles) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(roles);
        user.setKeys(new HashMap<>());
        Optional<User> existingUser = userRepository.findByEmail(email);
        if(existingUser.isPresent())
            throw  new RuntimeException("User already exists");
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username);
        if(user.isEmpty())
            throw new UsernameNotFoundException("User not found with email: " + username);
        return user.get();
    }
}
