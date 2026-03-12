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
@Order(1)
public class UserRolesMigration implements CommandLineRunner {

    private final MigrationFlagRepository migrationFlagRepository;

    private final JdbcTemplate jdbcTemplate;

    private static final String FLAG_NAME = "drop_user_roles_check_constraint";

    public UserRolesMigration(MigrationFlagRepository migrationFlagRepository, JdbcTemplate jdbcTemplate) {
        this.migrationFlagRepository = migrationFlagRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (migrationFlagRepository.findByName(FLAG_NAME).isPresent()) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS user_roles_role_check");

        jdbcTemplate.update("UPDATE user_roles SET role = 'USER' WHERE role NOT IN ('USER', 'ADMIN')");

        MigrationFlag flag = new MigrationFlag();
        flag.setName(FLAG_NAME);
        flag.setExecuted(true);
        flag.setExecutedAt(LocalDateTime.now());
        migrationFlagRepository.save(flag);

        System.out.println("Migracja user_roles wykonana pomyślnie.");
    }
}