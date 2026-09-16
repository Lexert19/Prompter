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
public class UserFileBase64PathMigration implements CommandLineRunner {

  private final MigrationFlagRepository migrationFlagRepository;
  private final JdbcTemplate jdbcTemplate;
  private static final String FLAG_NAME = "fix_user_file_base64path";

  public UserFileBase64PathMigration(MigrationFlagRepository migrationFlagRepository,
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

    boolean hasOld = columnExists("user_file", "base64path");
    boolean hasNew = columnExists("user_file", "base64_path");

    if (hasOld && hasNew) {
      jdbcTemplate.execute("UPDATE user_file SET base64_path = base64path WHERE base64_path IS NULL AND base64path IS NOT NULL");
      jdbcTemplate.execute("ALTER TABLE user_file DROP COLUMN base64path");
    } else if (hasOld && !hasNew) {
      jdbcTemplate.execute("ALTER TABLE user_file RENAME COLUMN base64path TO base64_path");
      jdbcTemplate.execute("ALTER TABLE user_file ALTER COLUMN base64_path DROP NOT NULL");
    } else if (hasOld) {
      jdbcTemplate.execute("ALTER TABLE user_file ALTER COLUMN base64path DROP NOT NULL");
    }

    if (columnExists("user_file", "base64_path")) {
      jdbcTemplate.execute("ALTER TABLE user_file ALTER COLUMN base64_path DROP NOT NULL");
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