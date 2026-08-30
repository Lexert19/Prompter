package com.example.promptengineering.migration;

import com.example.promptengineering.entity.MigrationFlag;
import com.example.promptengineering.repository.MigrationFlagRepository;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(4)
public class ProviderStrategyMigration implements CommandLineRunner {
  private final MigrationFlagRepository migrationFlagRepository;
  private final JdbcTemplate jdbcTemplate;
  private static final String FLAG_NAME = "add_provider_strategy_column";

  public ProviderStrategyMigration(MigrationFlagRepository migrationFlagRepository,
      JdbcTemplate jdbcTemplate) {
    this.migrationFlagRepository = migrationFlagRepository;
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (migrationFlagRepository.findByName(FLAG_NAME).isPresent()) return;

    if (!columnExists("model", "provider_strategy")) {
      jdbcTemplate.execute("ALTER TABLE model ADD COLUMN provider_strategy VARCHAR(50)");

      jdbcTemplate.update("UPDATE model SET provider_strategy = UPPER(provider)");

      jdbcTemplate.update("""
        UPDATE model SET provider_strategy = 'OPENAI' 
        WHERE provider_strategy IS NULL 
        OR UPPER(provider_strategy) NOT IN ('OPENAI','ANTHROPIC','GEMINI','OPENROUTER','NVIDIA','DEFAULT')
      """);

      jdbcTemplate.execute("ALTER TABLE model ALTER COLUMN provider_strategy SET DEFAULT 'OPENAI'");
      jdbcTemplate.execute("ALTER TABLE model ALTER COLUMN provider_strategy SET NOT NULL");
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