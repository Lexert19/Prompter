package com.example.promptengineering.service;

import java.util.*;

import com.example.promptengineering.dto.UserDto;
import com.example.promptengineering.exception.UserAlreadyExistsException;
import com.example.promptengineering.model.AppRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EncryptionService encryptionService;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final int maxEncryptedKeysLength;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            EncryptionService encryptionService,
            @Value("${app.max.encrypted.keys.length}") int maxEncryptedKeysLength) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.encryptionService = encryptionService;
        this.maxEncryptedKeysLength = maxEncryptedKeysLength;
    }

    public void setUserKeys(User user, Map<String, String> keys) {
        try {
            String json = objectMapper.writeValueAsString(keys);
            String encrypted = encryptionService.encrypt(json);
            if (encrypted.length() > maxEncryptedKeysLength) {
                throw new IllegalArgumentException(
                        "Encrypted keys too long (max " + maxEncryptedKeysLength + " characters)");
            }
            user.setEncryptedKeys(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error saving keys", e);
        }
    }

    public void appendKeyToMap(User user, String keyName, String keyValue) {
        Map<String, String> keys = getUserKeys(user);
        keys.put(keyName, keyValue);
        this.setUserKeys(user, keys);
    }

    public Map<String, String> getUserKeys(User user) {
        if (user.getEncryptedKeys() == null || user.getEncryptedKeys().isEmpty()) {
            return new HashMap<>();
        }
        try {
            if (user.getEncryptedKeys().length() > maxEncryptedKeysLength) {
                throw new IllegalArgumentException(
                        "Encrypted keys too long (max " + maxEncryptedKeysLength + " characters)");
            }
            String decrypted = encryptionService.decrypt(user.getEncryptedKeys());
            return objectMapper.readValue(decrypted, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Error reading keys", e);
        }
    }

    public User findUserByEmail(String email) throws Exception {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new Exception("User does not exists.");
        }
        return user.get();
    }

    private void checkUserNotExists(String email) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("User already exists with email: " + email);
        }
    }

    private User createAndSaveUser(String email, String encodedPassword, List<AppRole> roles) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRoles(roles);
        user.setEncryptedKeys(null);
        return userRepository.save(user);
    }

    public User createUser(String email, String rawPassword, List<AppRole> roles) throws UserAlreadyExistsException {
        checkUserNotExists(email);
        String encodedPassword = passwordEncoder.encode(rawPassword);
        return createAndSaveUser(email, encodedPassword, roles);
    }

    public User createUser(String email, List<AppRole> roles) throws UserAlreadyExistsException {
        checkUserNotExists(email);
        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(randomPassword);
        return createAndSaveUser(email, encodedPassword, roles);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + username);
        }
        return user.get();
    }

    public UserDto mapUserToDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setPoints(user.getPoints());
        return userDto;
    }
}
