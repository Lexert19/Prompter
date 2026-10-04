package com.example.promptengineering.service;

import com.example.promptengineering.dto.RegisterRequest;
import com.example.promptengineering.exception.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.util.*;

import com.example.promptengineering.dto.UserDto;
import com.example.promptengineering.exception.UserAlreadyExistsException;
import com.example.promptengineering.model.AppRole;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final SecureRandom random = new SecureRandom();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
            EncryptionService encryptionService,
            @Value("${app.max.encrypted.keys.length}") int maxEncryptedKeysLength) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.encryptionService = encryptionService;
        this.maxEncryptedKeysLength = maxEncryptedKeysLength;
    }

    public User registerUser(RegisterRequest request) {
        return createUser(request.email(), request.password(), List.of(AppRole.USER));
    }

    @Transactional
    public String generateApiToken(User user) {
        String token;
        do {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } while (userRepository.existsByApiToken(token));

        user.setApiToken(token);
        userRepository.save(user);
        return token;
    }

    @Transactional
    public String rotateApiToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return generateApiToken(user);
    }

    public void setUserKeys(User user, Map<String, String> keys)
            throws ValidationException {
        String encrypted;
        try {
            String json = objectMapper.writeValueAsString(keys);
            encrypted = encryptionService.encrypt(json);
        } catch (Exception e) {
            throw new RuntimeException("Error saving keys", e);
        }
        if (encrypted.length() > maxEncryptedKeysLength) {
            throw new ValidationException("Encrypted keys too long (max "
                    + maxEncryptedKeysLength + " characters)");
        }
        user.setEncryptedKeys(encrypted);
    }

    @Transactional
    public void appendKeyToMap(User user, String keyName, String keyValue)
            throws ValidationException {
        Map<String, String> keys = getUserKeys(user);
        keys.put(keyName, keyValue);
        this.setUserKeys(user, keys);
        userRepository.save(user);
    }

    public Map<String, String> getUserKeys(User user) {
        if (user.getEncryptedKeys() == null || user.getEncryptedKeys().isEmpty()) {
            return new HashMap<>();
        }

        String decrypted;
        try {
            decrypted = encryptionService.decrypt(user.getEncryptedKeys());
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt keys for user " + user.getId(),
                    e);
        }

        try {
            return objectMapper.readValue(decrypted,
                    new TypeReference<Map<String, String>>() {
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse keys for user " + user.getId(),
                    e);
        }
    }

    public User findUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User does not exists.");
        }
        return user.get();
    }

    private User createAndSaveUser(String email, String encodedPassword,
                                   List<AppRole> roles) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);
        user.setRoles(new ArrayList<>(roles));
        user.setEncryptedKeys(null);
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new UserAlreadyExistsException(
                    "User already exists with email: " + email);
        }
    }

    public User createUser(String email, String rawPassword, List<AppRole> roles) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        return createAndSaveUser(email, encodedPassword, roles);
    }

    public User createUser(String email, List<AppRole> roles) {
        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(randomPassword);
        return createAndSaveUser(email, encodedPassword, roles);
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(@NonNull String username)
            throws UsernameNotFoundException {
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
