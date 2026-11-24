package com.example.promptengineering.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@RequiredArgsConstructor
public class IpHitBatchWriter {
    private final JdbcTemplate jdbc;
    private final BlockingQueue<Object[]> queue = new LinkedBlockingQueue<>(10000);

    public void offer(long millis, String ip) {
        if (ip == null) return;
        queue.offer(new Object[]{millis, ip});
    }

    public void clear() {
        queue.clear();
    }

    @Scheduled(fixedDelay = 500)
    public void flush() {
        List<Object[]> batch = new ArrayList<>();
        queue.drainTo(batch, 500);
        if (batch.isEmpty()) return;
        jdbc.batchUpdate(
                "INSERT INTO ip_hits (time, ip) VALUES (?, ?::inet)",
                batch,
                batch.size(),
                (ps, row) -> {
                    ps.setTimestamp(1, new Timestamp((Long) row[0]));
                    ps.setString(2, (String) row[1]);
                });
    }

    @PreDestroy
    public void flushOnShutdown() {
        flush();
    }
}