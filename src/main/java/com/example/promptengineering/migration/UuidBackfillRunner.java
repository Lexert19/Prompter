package com.example.promptengineering.migration;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
@RequiredArgsConstructor
public class UuidBackfillRunner implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        try (Connection conn = dataSource.getConnection();
                Statement st = conn.createStatement()) {

            st.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto");

            st.execute("UPDATE chat SET uuid = gen_random_uuid() WHERE uuid IS NULL");
            st.execute("UPDATE message SET uuid = gen_random_uuid() WHERE uuid IS NULL");
        }
    }
}
