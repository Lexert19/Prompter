package com.example.promptengineering.service;

import com.example.promptengineering.dto.IpHit;
import com.example.promptengineering.repository.IpHitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IpHitService {

    private final IpHitRepository repo;

    @Async("taskExecutor")
    public void logAsync(IpHit hit) {
        try {
            repo.insert(hit);
        } catch (Exception ex) {
            log.warn("Failed to save ip_hits for ip={}", hit.ip(), ex);
        }
    }
}