package com.example.promptengineering.restController;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/metrics")
@RequiredArgsConstructor
public class MetricsController {
    private final JdbcTemplate jdbc;

    @GetMapping("/{name}")
    public List<Map<String, Object>> history(@PathVariable String name,
                                             @RequestParam(defaultValue = "1h") String range) {
        return jdbc.queryForList("""
                SELECT time, value FROM metric_point
                WHERE name = ? AND time > now() - ?::interval
                ORDER BY time
                """, name, range);
    }
}
