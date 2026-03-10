package com.example.promptengineering.restController;

import com.example.promptengineering.entity.SharedKey;
import com.example.promptengineering.repository.SharedKeyRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/shared-keys")
public class AdminSharedKeyController {
    private final SharedKeyRepository repository;

    public AdminSharedKeyController(SharedKeyRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public SharedKey addSharedKey(@RequestBody SharedKey key) {
        key.setWorking(true);
        key.setUsageCount(0);
        return repository.save(key);
    }
}
