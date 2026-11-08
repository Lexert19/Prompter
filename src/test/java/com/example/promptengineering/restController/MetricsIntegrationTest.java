package com.example.promptengineering.restController;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.MetricsService;
import com.example.promptengineering.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MetricsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MetricsService metricsService;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    private User adminUser;
    private User normalUser;
    private String adminEmail;
    private String userEmail;
    private String uniqueName;

    @BeforeEach
    void setUp() {
        adminEmail = "metrics-admin-" + UUID.randomUUID() + "@test.com";
        userEmail = "metrics-user-" + UUID.randomUUID() + "@test.com";
        adminUser = userService.createUser(adminEmail, "adminPass",
                List.of(AppRole.ADMIN));
        normalUser = userService.createUser(userEmail, "userPass", List.of(AppRole.USER));
        uniqueName = "test.metric." + UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        jdbc.update("DELETE FROM metric_point WHERE name = ?", uniqueName);

        if (adminUser != null && adminUser.getId() != null) {
            userRepository.delete(adminUser);
        }
        if (normalUser != null && normalUser.getId() != null) {
            userRepository.delete(normalUser);
        }
    }

    @Test
    void shouldPersistMetricWhenRecordCalled() {
        metricsService.record(uniqueName, 42.5);

        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT name, value FROM metric_point WHERE name = ?", uniqueName);

        assertThat(rows).hasSize(1);
        assertThat(((Number) rows.getFirst().get("value")).doubleValue()).isEqualTo(42.5);
        assertThat(rows.getFirst().get("name")).isEqualTo(uniqueName);
    }

    @Test
    void shouldNotThrowWhenRecordFails() {
        String tooLong = "x".repeat(500);
        assertThatCode(() -> metricsService.record(tooLong, 1.0))
                .doesNotThrowAnyException();

        assertThat(
                jdbc.queryForList("SELECT 1 FROM metric_point WHERE name = ?", tooLong))
                .isEmpty();
    }

    @Test
    void shouldReturnHistoryForAdmin() throws Exception {
        metricsService.record(uniqueName, 10.0);
        metricsService.record(uniqueName, 20.0);

        mockMvc.perform(get("/api/admin/metrics/{name}", uniqueName).param("range", "1h")
                .with(user(userService.loadUserByUsername(adminEmail))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].value").value(10.0))
                .andExpect(jsonPath("$[1].value").value(20.0));
    }

    @Test
    void shouldReturnAscendingByTime() throws Exception {
        jdbc.update(
                "INSERT INTO metric_point(time, name, value) VALUES (now() - INTERVAL '2 min', ?, 1.0)",
                uniqueName);
        jdbc.update(
                "INSERT INTO metric_point(time, name, value) VALUES (now() - INTERVAL '1 min', ?, 2.0)",
                uniqueName);
        jdbc.update("INSERT INTO metric_point(time, name, value) VALUES (now(), ?, 3.0)",
                uniqueName);

        mockMvc.perform(get("/api/admin/metrics/{name}", uniqueName).param("range", "1h")
                .with(user(userService.loadUserByUsername(adminEmail))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].value").value(1.0))
                .andExpect(jsonPath("$[2].value").value(3.0));
    }

    @Test
    void shouldReturnEmptyForUnknownMetric() throws Exception {
        mockMvc.perform(
                get("/api/admin/metrics/{name}", "does.not.exist." + UUID.randomUUID())
                        .with(user(userService.loadUserByUsername(adminEmail))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldFilterByRangeParameter() throws Exception {
        jdbc.update(
                "INSERT INTO metric_point(time, name, value) VALUES (now() - INTERVAL '2 hours', ?, 1.0)",
                uniqueName);
        jdbc.update("INSERT INTO metric_point(time, name, value) VALUES (now(), ?, 2.0)",
                uniqueName);

        mockMvc.perform(get("/api/admin/metrics/{name}", uniqueName).param("range", "1h")
                .with(user(userService.loadUserByUsername(adminEmail))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].value").value(2.0));
    }

    @Test
    void shouldAcceptHumanRangeFormats() throws Exception {
        for (String range : List.of("30m", "1h", "7d")) {
            mockMvc.perform(
                    get("/api/admin/metrics/{name}", uniqueName).param("range", range)
                            .with(user(userService.loadUserByUsername(adminEmail))))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void shouldReturn403ForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/{name}", uniqueName)
                .with(user(userService.loadUserByUsername(userEmail))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn401ForAnonymous() throws Exception {
        mockMvc.perform(get("/api/admin/metrics/{name}", uniqueName))
                .andExpect(status().isUnauthorized());
    }
}
