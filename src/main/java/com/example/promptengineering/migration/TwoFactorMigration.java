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
@Order(3)
public class TwoFactorMigration implements CommandLineRunner {

    private final MigrationFlagRepository migrationFlagRepository;
    private final JdbcTemplate jdbcTemplate;
    private static final String FLAG_NAME = "add_two_factor_columns";

    public TwoFactorMigration(MigrationFlagRepository migrationFlagRepository, JdbcTemplate jdbcTemplate) {
        this.migrationFlagRepository = migrationFlagRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (migrationFlagRepository.findByName(FLAG_NAME).isPresent()) {
            return;
        }

        if (!columnExists("app_user", "two_factor_enabled")) {
            jdbcTemplate.execute("ALTER TABLE app_user ADD COLUMN two_factor_enabled BOOLEAN DEFAULT FALSE");
            jdbcTemplate.update("UPDATE app_user SET two_factor_enabled = FALSE WHERE two_factor_enabled IS NULL");
        } else {
            jdbcTemplate.update("UPDATE app_user SET two_factor_enabled = FALSE WHERE two_factor_enabled IS NULL");
        }

        if (!columnExists("app_user", "two_factor_email")) {
            jdbcTemplate.execute("ALTER TABLE app_user ADD COLUMN two_factor_email VARCHAR(255)");
        }

        MigrationFlag flag = new MigrationFlag();
        flag.setName(FLAG_NAME);
        flag.setExecuted(true);
        flag.setExecutedAt(LocalDateTime.now());
        migrationFlagRepository.save(flag);
    }

    private boolean columnExists(String tableName, String columnName) {
        String sql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = ? AND column_name = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, tableName, columnName);
        return count != null && count > 0;
    }
}
