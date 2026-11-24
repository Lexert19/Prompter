package com.example.promptengineering.repository;

import com.example.promptengineering.dto.IpHit;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class IpHitRepository {

    private final JdbcTemplate jdbc;

    public void insert(IpHit hit) {
        jdbc.update(
                "INSERT INTO ip_hits (time, ip) VALUES (?, ?::inet)",
                Timestamp.from(hit.time()),
                hit.ip()
        );
    }

    public void insertBatch(List<IpHit> hits) {
        if (hits.isEmpty()) return;
        jdbc.batchUpdate(
                "INSERT INTO ip_hits (time, ip) VALUES (?, ?::inet)",
                hits,
                hits.size(),
                (ps, hit) -> {
                    ps.setTimestamp(1, Timestamp.from(hit.time()));
                    ps.setString(2, hit.ip());
                });
    }

    public List<Map<String, Object>> topIpsLast24h() {
        return jdbc.queryForList(
                "SELECT ip, COUNT(*) AS cnt FROM ip_hits " +
                        "WHERE time > now() - INTERVAL '24 hours' " +
                        "GROUP BY ip ORDER BY cnt DESC LIMIT 100"
        );
    }

    public List<Map<String, Object>> topIpsByHours(int hours) {
        return jdbc.queryForList(
                "SELECT ip::text AS ip, COUNT(*) AS cnt FROM ip_hits " +
                        "WHERE time > now() - (? || ' hours')::interval " +
                        "GROUP BY ip ORDER BY cnt DESC LIMIT 100",
                String.valueOf(hours)
        );
    }

    public List<Map<String, Object>> uniqueIpsPerHour(int hours) {
        return jdbc.queryForList(
                "SELECT time_bucket('1 hour', time) AS bucket, COUNT(DISTINCT ip) AS unique_ips " +
                        "FROM ip_hits WHERE time > now() - (? || ' hours')::interval " +
                        "GROUP BY bucket ORDER BY bucket",
                String.valueOf(hours)
        );
    }

    public List<Map<String, Object>> uniqueIpsPerHour() {
        return jdbc.queryForList(
                "SELECT time_bucket('1 hour', time) AS bucket, COUNT(DISTINCT ip) AS unique_ips " +
                        "FROM ip_hits WHERE time > now() - INTERVAL '24 hours' " +
                        "GROUP BY bucket ORDER BY bucket"
        );
    }

    public List<Map<String, Object>> findPage(String ip, LocalDateTime from, LocalDateTime to, int limit, int offset) {
        StringBuilder sql = new StringBuilder("SELECT time, ip::text AS ip FROM ip_hits WHERE 1 = 1 ");
        List<Object> params = new java.util.ArrayList<>();
        if (ip != null && !ip.isBlank()) {
            sql.append("AND ip = ?::inet ");
            params.add(ip);
        }
        if (from != null) {
            sql.append("AND time >= ? ");
            params.add(Timestamp.valueOf(from));
        }
        if (to != null) {
            sql.append("AND time <= ? ");
            params.add(Timestamp.valueOf(to));
        }
        sql.append("ORDER BY time DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return jdbc.queryForList(sql.toString(), params.toArray());
    }

    public long countFiltered(String ip, LocalDateTime from, LocalDateTime to) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM ip_hits WHERE 1 = 1 ");
        List<Object> params = new java.util.ArrayList<>();
        if (ip != null && !ip.isBlank()) {
            sql.append("AND ip = ?::inet ");
            params.add(ip);
        }
        if (from != null) {
            sql.append("AND time >= ? ");
            params.add(Timestamp.valueOf(from));
        }
        if (to != null) {
            sql.append("AND time <= ? ");
            params.add(Timestamp.valueOf(to));
        }
        Long count = jdbc.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0;
    }
}