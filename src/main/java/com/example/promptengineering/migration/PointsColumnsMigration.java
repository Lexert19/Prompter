package com.example.promptengineering.migration;

import com.example.promptengineering.entity.MigrationFlag;
import com.example.promptengineering.repository.MigrationFlagRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
@Order(2)
public class PointsColumnsMigration implements CommandLineRunner {

    private final MigrationFlagRepository migrationFlagRepository;
    private final JdbcTemplate jdbcTemplate;
    private static final String FLAG_NAME = "add_points_columns";

    public PointsColumnsMigration(MigrationFlagRepository migrationFlagRepository,
            JdbcTemplate jdbcTemplate) {
        this.migrationFlagRepository = migrationFlagRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (migrationFlagRepository.findByName(FLAG_NAME).isPresent()) {
            return;
        }

        if (!columnExists("app_user", "points")) {
            jdbcTemplate
                    .execute("ALTER TABLE app_user ADD COLUMN points DOUBLE PRECISION");
            jdbcTemplate.update("UPDATE app_user SET points = 0 WHERE points IS NULL");
            jdbcTemplate.execute("ALTER TABLE app_user ALTER COLUMN points SET NOT NULL");
        } else {
            jdbcTemplate.update("UPDATE app_user SET points = 0 WHERE points IS NULL");
        }

        if (!columnExists("model", "points_per_input")) {
            jdbcTemplate.execute(
                    "ALTER TABLE model ADD COLUMN points_per_input DOUBLE PRECISION");
            jdbcTemplate.update(
                    "UPDATE model SET points_per_input = 0 WHERE points_per_input IS NULL");
            jdbcTemplate.execute(
                    "ALTER TABLE model ALTER COLUMN points_per_input SET NOT NULL");
        } else {
            jdbcTemplate.update(
                    "UPDATE model SET points_per_input = 0 WHERE points_per_input IS NULL");
        }

        if (!columnExists("model", "points_per_output")) {
            jdbcTemplate.execute(
                    "ALTER TABLE model ADD COLUMN points_per_output DOUBLE PRECISION");
            jdbcTemplate.update(
                    "UPDATE model SET points_per_output = 0 WHERE points_per_output IS NULL");
            jdbcTemplate.execute(
                    "ALTER TABLE model ALTER COLUMN points_per_output SET NOT NULL");
        } else {
            jdbcTemplate.update(
                    "UPDATE model SET points_per_output = 0 WHERE points_per_output IS NULL");
        }

        MigrationFlag flag = new MigrationFlag();
        flag.setName(FLAG_NAME);
        flag.setExecuted(true);
        flag.setExecutedAt(LocalDateTime.now());
        migrationFlagRepository.save(flag);
    }

    private boolean columnExists(String tableName, String columnName) {
        String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName,
                columnName);
        return count != null && count > 0;
    }
}
