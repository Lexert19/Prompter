package com.example.promptengineering.filter;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.IpHitBatchWriter;
import com.example.promptengineering.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IpLoggingFilterIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private IpHitBatchWriter writer;

    private User adminUser;
    private User normalUser;
    private RequestPostProcessor asAdmin;
    private RequestPostProcessor asRegularUser;

    @BeforeEach
    void setUp() {
        writer.clear();
        String adminEmail = "filter-admin-" + UUID.randomUUID() + "@test.com";
        String userEmail = "filter-user-" + UUID.randomUUID() + "@test.com";

        adminUser = userService.createUser(adminEmail, "adminPass", List.of(AppRole.ADMIN));
        normalUser = userService.createUser(userEmail, "userPass", List.of(AppRole.USER));

        asAdmin = user(userService.loadUserByUsername(adminEmail));
        asRegularUser = user(userService.loadUserByUsername(userEmail));
    }

    @AfterEach
    void tearDown() {
        if (adminUser != null) userRepository.delete(adminUser);
        if (normalUser != null) userRepository.delete(normalUser);
    }

    @Test
    void filter_offersAndBatchWriterFlushes() throws Exception {
        String filterIp = "198.51.100." + (Math.abs(UUID.randomUUID().hashCode()) % 200 + 10);
        jdbc.update("DELETE FROM ip_hits WHERE ip = ?::inet", filterIp);

        mockMvc.perform(get("/api/account/keys")
                        .with(asRegularUser)
                        .with(req -> {
                            req.setRemoteAddr(filterIp);
                            return req;
                        }))
                .andExpect(status().isOk());

        Integer before = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ip_hits WHERE ip = ?::inet",
                Integer.class, filterIp);

        writer.flush();

        Integer after = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ip_hits WHERE ip = ?::inet",
                Integer.class, filterIp);

        assertTrue(after != null && after >= 1);
        assertTrue(after > (before == null ? 0 : before));

        jdbc.update("DELETE FROM ip_hits WHERE ip = ?::inet", filterIp);
    }

    @Test
    void writer_flushesBatchOf500() {
        String ip = "203.0.113." + (Math.abs(UUID.randomUUID().hashCode()) % 200 + 10);
        jdbc.update("DELETE FROM ip_hits WHERE ip = ?::inet", ip);

        long now = System.currentTimeMillis();
        for (int i = 0; i < 500; i++) {
            writer.offer(now, ip);
        }
        writer.flush();

        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM ip_hits WHERE ip = ?::inet",
                Integer.class, ip);
        assertTrue(count != null && count == 500);

        jdbc.update("DELETE FROM ip_hits WHERE ip = ?::inet", ip);
    }
}