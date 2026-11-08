package com.example.promptengineering.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {
    private final JdbcTemplate jdbc;

    public void record(String name, double value) {
        try {
            jdbc.update(
                    "INSERT INTO metric_point(time, name, value) VALUES (now(), ?, ?)",
                    name, value);
        } catch (Exception e) {
            log.warn("Metric write failed: {}", e.getMessage());
        }
    }
}
