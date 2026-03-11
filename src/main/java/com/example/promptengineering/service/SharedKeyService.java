package com.example.promptengineering.service;

import com.example.promptengineering.entity.SharedKey;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.SharedKeyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class SharedKeyService {
    private final SharedKeyRepository sharedKeyRepository;
    private final Random random = new Random();

    @Autowired
    private EncryptionService encryptionService;

    public SharedKeyService(SharedKeyRepository sharedKeyRepository) {
        this.sharedKeyRepository = sharedKeyRepository;
    }

    public SharedKey getRandomWorkingKey(String provider) {
        List<SharedKey> workingKeys = sharedKeyRepository.findByProvider(provider);
        if (workingKeys.isEmpty()) {
            //throw new RuntimeException("No working keys for provider: " + provider);
        }
        SharedKey key = workingKeys.get(random.nextInt(workingKeys.size()));
        key.setUsageCount(key.getUsageCount() + 1);
        return sharedKeyRepository.save(key);
    }

    @Transactional
    public void addKey(String provider, String rawKey, User owner) {
        String encrypted = encryptionService.encrypt(rawKey);
        SharedKey key = new SharedKey(provider, encrypted);
        sharedKeyRepository.save(key);
    }
}
