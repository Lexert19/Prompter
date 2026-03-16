package com.example.promptengineering.service;

import com.example.promptengineering.dto.SharedKeyInfoDto;
import com.example.promptengineering.entity.SharedKey;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.SharedKeyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class SharedKeyService {
    private final SharedKeyRepository sharedKeyRepository;
    private final Random random = new Random();

    @Autowired
    private EncryptionService encryptionService;

    public SharedKeyService(SharedKeyRepository sharedKeyRepository) {
        this.sharedKeyRepository = sharedKeyRepository;
    }

    public String getRandomWorkingKey(String provider) {
        List<SharedKey> workingKeys = sharedKeyRepository.findByProviderAndWorkingTrue(provider);
        if (workingKeys.isEmpty()) {
            throw new RuntimeException("No working keys for provider: " + provider);
        }
        SharedKey key = workingKeys.get(random.nextInt(workingKeys.size()));
        key.setUsageCount(key.getUsageCount() + 1);
        sharedKeyRepository.save(key);
        return encryptionService.decrypt(key.getKeyValue());
    }

    public SharedKey getRandomWorkingKeyEntity(String provider) {
        List<SharedKey> workingKeys = sharedKeyRepository.findByProviderAndWorkingTrue(provider);
        if (workingKeys.isEmpty()) {
            throw new RuntimeException("No working keys for provider: " + provider);
        }
        SharedKey key = workingKeys.get(random.nextInt(workingKeys.size()));
        key.setUsageCount(key.getUsageCount() + 1);
        sharedKeyRepository.save(key);
        return key;
    }

    public List<SharedKeyInfoDto> getAllKeys() {
        return sharedKeyRepository.findAll().stream()
                .map(key -> new SharedKeyInfoDto(
                        key.getId(),
                        key.getProvider(),
                        key.isWorking(),
                        key.getUsageCount()))
                .collect(Collectors.toList());
    }

    public boolean deleteKey(Long id) {
        if (sharedKeyRepository.existsById(id)) {
            sharedKeyRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public void addKey(String provider, String rawKey) {
        String encrypted = encryptionService.encrypt(rawKey);
        SharedKey key = new SharedKey(provider, encrypted);
        key.setWorking(true);
        key.setUsageCount(0);
        sharedKeyRepository.save(key);
    }
}
