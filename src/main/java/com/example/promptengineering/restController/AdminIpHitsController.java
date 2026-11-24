package com.example.promptengineering.restController;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.sql.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/admin/ip-hits")
@RequiredArgsConstructor
public class AdminIpHitsController {
    private final JdbcTemplate jdbc;
    private static final Pattern RANGE = Pattern.compile("^\\d+[mhd]$");

    @GetMapping
    public Map<String, List<Long>> list(
            @RequestParam(defaultValue = "1h") String range,
            @RequestParam(required = false) String ip) {
        validateRange(range);
        String interval = toInterval(range);
        if (ip == null) {
            return jdbc.query("""
                SELECT host(ip) AS ip, array_agg((EXTRACT(EPOCH FROM time) * 1000)::bigint ORDER BY time DESC) AS times
                FROM (
                    SELECT time, ip FROM ip_hits
                    WHERE ip IS NOT NULL AND time > now() - ?::interval
                    ORDER BY time DESC LIMIT 1000
                ) s
                GROUP BY host(ip)
                """, (rs, rowNum) -> Map.entry(rs.getString("ip"), toLongList(rs.getArray("times"))),
                    interval).stream().collect(LinkedHashMap::new,
                    (m, e) -> m.put(e.getKey(), e.getValue()),
                    LinkedHashMap::putAll);
        }
        validateIp(ip);
        return Map.of(ip, byIpTimes(ip, interval));
    }

    @GetMapping("/top")
    public Map<String, Long> top(
            @RequestParam(defaultValue = "1h") String range,
            @RequestParam(defaultValue = "10") int limit) {
        validateRange(range);
        int safeLimit = Math.clamp(limit, 1, 100);
        return jdbc.query("""
            SELECT host(ip) AS ip, COUNT(*) AS hits FROM ip_hits
            WHERE ip IS NOT NULL AND time > now() - ?::interval
            GROUP BY host(ip) ORDER BY hits DESC LIMIT ?
            """, (rs, rowNum) -> Map.entry(rs.getString("ip"), rs.getLong("hits")),
                toInterval(range), safeLimit).stream().collect(LinkedHashMap::new,
                (m, e) -> m.put(e.getKey(), e.getValue()),
                LinkedHashMap::putAll);
    }

    @GetMapping("/hourly")
    public List<List<Long>> hourly(
            @RequestParam(defaultValue = "24h") String range,
            @RequestParam(required = false) String ip) {
        validateRange(range);
        String interval = toInterval(range);
        if (ip == null) {
            return jdbc.query("""
                SELECT (EXTRACT(EPOCH FROM date_trunc('hour', time)) * 1000)::bigint AS bucket, COUNT(*) AS hits FROM ip_hits
                WHERE time > now() - ?::interval
                GROUP BY bucket ORDER BY bucket
                """, (rs, rowNum) -> List.of(rs.getLong("bucket"), rs.getLong("hits")),
                    interval);
        }
        validateIp(ip);
        return jdbc.query("""
            SELECT (EXTRACT(EPOCH FROM date_trunc('hour', time)) * 1000)::bigint AS bucket, COUNT(*) AS hits FROM ip_hits
            WHERE ip = ?::inet AND time > now() - ?::interval
            GROUP BY bucket ORDER BY bucket
            """, (rs, rowNum) -> List.of(rs.getLong("bucket"), rs.getLong("hits")),
                ip, interval);
    }

    @GetMapping("/{ip}")
    public Map<String, Object> byIp(
            @PathVariable String ip,
            @RequestParam(defaultValue = "1h") String range) {
        validateRange(range);
        validateIp(ip);
        return Map.of("ip", ip, "timestamps", byIpTimes(ip, toInterval(range)));
    }

    private List<Long> byIpTimes(String ip, String interval) {
        List<Long> result = jdbc.queryForObject("""
            SELECT array_agg((EXTRACT(EPOCH FROM time) * 1000)::bigint ORDER BY time DESC) AS times
            FROM (
                SELECT time FROM ip_hits
                WHERE ip = ?::inet AND time > now() - ?::interval
                ORDER BY time DESC LIMIT 1000
            ) s
            """, (rs, rowNum) -> toLongList(rs.getArray("times")), ip, interval);
        return result == null ? List.of() : result;
    }

    private List<Long> toLongList(Array array) {
        if (array == null) return List.of();
        try {
            Object[] arr = (Object[]) array.getArray();
            List<Long> out = new ArrayList<>(arr.length);
            for (Object o : arr) {
                if (o instanceof Number n) out.add(n.longValue());
            }
            return out;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read timestamps", e);
        }
    }

    private void validateRange(String range) {
        if (range == null || !RANGE.matcher(range).matches()) {
            throw new IllegalArgumentException("Invalid range: " + range);
        }
    }

    private void validateIp(String ip) {
        try {
            InetAddress.getByName(ip);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid IP: " + ip);
        }
    }

    private String toInterval(String range) {
        char unit = range.charAt(range.length() - 1);
        String num = range.substring(0, range.length() - 1);
        return switch (unit) {
            case 'm' -> num + " minutes";
            case 'h' -> num + " hours";
            case 'd' -> num + " days";
            default -> throw new IllegalArgumentException("Invalid range: " + range);
        };
    }
}