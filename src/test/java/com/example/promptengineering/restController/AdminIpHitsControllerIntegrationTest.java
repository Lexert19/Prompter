package com.example.promptengineering.restController;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminIpHitsControllerIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/admin/ip-hits";

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User normalUser;
    private String adminEmail;
    private String userEmail;
    private RequestPostProcessor asAdmin;
    private RequestPostProcessor asRegularUser;

    private String ipA;
    private String ipB;
    private String ipC;
    private String filterIp;

    @BeforeEach
    void setUp() {
        int suffix = Math.abs(UUID.randomUUID().hashCode()) % 200 + 10;
        ipA = "203.0.113." + suffix;
        ipB = "198.51.100." + suffix;
        ipC = "192.0.2." + suffix;
        filterIp = "198.51.100." + ((suffix + 1) % 250);

        adminEmail = "iphits-admin-" + UUID.randomUUID() + "@test.com";
        userEmail = "iphits-user-" + UUID.randomUUID() + "@test.com";

        adminUser = userService.createUser(adminEmail, "adminPass", List.of(AppRole.ADMIN));
        normalUser = userService.createUser(userEmail, "userPass", List.of(AppRole.USER));

        asAdmin = user(userService.loadUserByUsername(adminEmail));
        asRegularUser = user(userService.loadUserByUsername(userEmail));
    }

    @AfterEach
    void tearDown() {
        for (String ip : List.of(ipA, ipB, ipC, filterIp)) {
            jdbc.update("DELETE FROM ip_hits WHERE ip = ?::inet", ip);
        }
        jdbc.update("DELETE FROM ip_hits WHERE ip = '127.0.0.1'::inet");
        if (adminUser != null) userRepository.delete(adminUser);
        if (normalUser != null) userRepository.delete(normalUser);
    }

    @Test
    void anonymous_cannotList() throws Exception {
        mockMvc.perform(get(BASE_URL)).andExpect(status().isUnauthorized());
    }

    @Test
    void anonymous_cannotGetTop() throws Exception {
        mockMvc.perform(get(BASE_URL + "/top")).andExpect(status().isUnauthorized());
    }

    @Test
    void anonymous_cannotGetHourly() throws Exception {
        mockMvc.perform(get(BASE_URL + "/hourly")).andExpect(status().isUnauthorized());
    }

    @Test
    void anonymous_cannotGetByIp() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{ip}", ipA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularUser_cannotList() throws Exception {
        mockMvc.perform(get(BASE_URL).with(asRegularUser))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUser_cannotGetTop() throws Exception {
        mockMvc.perform(get(BASE_URL + "/top").with(asRegularUser))
                .andExpect(status().isForbidden());
    }

    @Test
    void regularUser_cannotGetByIp() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{ip}", ipA).with(asRegularUser))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_list_filtersByIp() throws Exception {
        seed(ipA, 2, 5);
        seed(ipB, 1, 5);

        mockMvc.perform(get(BASE_URL).param("ip", ipA).param("range", "1h").with(asAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['" + ipA + "']", hasSize(2)))
                .andExpect(jsonPath("$", not(hasKey(ipB))));
    }

    @Test
    void admin_list_filtersByRange() throws Exception {
        seed(ipA, 2, 5);
        seed(ipA, 1, 120);

        mockMvc.perform(get(BASE_URL).param("ip", ipA).param("range", "1h").with(asAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['" + ipA + "']", hasSize(2)));

        mockMvc.perform(get(BASE_URL).param("ip", ipA).param("range", "3h").with(asAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['" + ipA + "']", hasSize(3)));
    }

    @Test
    void admin_list_ordersByTimeDesc() throws Exception {
        seed(ipA, 1, 30);
        seed(ipA, 1, 10);
        seed(ipA, 1, 1);

        MvcResult result = mockMvc.perform(get(BASE_URL).param("ip", ipA).param("range", "1h").with(asAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['" + ipA + "']", hasSize(3)))
                .andReturn();

        Map<String, List<Long>> map = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<LinkedHashMap<String, List<Long>>>() {});
        List<Long> times = map.get(ipA);
        assertTrue(times.get(0) > times.get(1));
        assertTrue(times.get(1) > times.get(2));
    }

    @Test
    void admin_list_invalidRange_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL).param("range", "banana").with(asAdmin))
                .andExpect(status().isBadRequest());
    }

    @Test
    void admin_list_invalidIpFilter_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL).param("ip", "not-an-ip").with(asAdmin))
                .andExpect(status().isBadRequest());
    }

    @Test
    void admin_top_returnsCountsOrdered() throws Exception {
        seed(ipA, 10, 5);
        seed(ipB, 7, 5);
        seed(ipC, 2, 5);

        MvcResult result = mockMvc.perform(get(BASE_URL + "/top").param("range", "1h").param("limit", "10").with(asAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasKey(ipA)))
                .andExpect(jsonPath("$", hasKey(ipB)))
                .andExpect(jsonPath("$", hasKey(ipC)))
                .andReturn();

        Map<String, Long> top = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<LinkedHashMap<String, Long>>() {});
        List<String> keys = List.copyOf(top.keySet());
        assertTrue(keys.indexOf(ipA) < keys.indexOf(ipB));
        assertTrue(keys.indexOf(ipB) < keys.indexOf(ipC));
    }

    @Test
    void admin_top_respectsLimit() throws Exception {
        seed(ipA, 5, 5);
        seed(ipB, 4, 5);
        seed(ipC, 3, 5);

        MvcResult result = mockMvc.perform(get(BASE_URL + "/top").param("range", "1h").param("limit", "2").with(asAdmin))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, Long> top = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<LinkedHashMap<String, Long>>() {});
        assertTrue(top.size() == 2);
        assertTrue(top.containsKey(ipA));
    }

    @Test
    void admin_top_respectsRange() throws Exception {
        seed(ipA, 5, 120);
        seed(ipB, 2, 5);

        mockMvc.perform(get(BASE_URL + "/top").param("range", "1h").with(asAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasKey(ipB)))
                .andExpect(jsonPath("$", not(hasKey(ipA))));
    }

    @Test
    void admin_hourly_bucketsByHour() throws Exception {
        seed(ipA, 1, 70);
        seed(ipA, 2, 5);

        MvcResult result = mockMvc.perform(get(BASE_URL + "/hourly").param("range", "3h").param("ip", ipA).with(asAdmin))
                .andExpect(status().isOk())
                .andReturn();

        List<List<Long>> hourly = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<List<Long>>>() {});
        assertTrue(hourly.size() == 2);
        assertTrue(hourly.get(0).get(0) < hourly.get(1).get(0));
        for (List<Long> bucket : hourly) {
            assertTrue(bucket.size() == 2);
            assertTrue(bucket.get(0) > 1_000_000_000_000L);
            assertTrue(bucket.get(1) >= 1);
        }
    }

    @Test
    void admin_byIp_returnsOnlyThatIp() throws Exception {
        seed(ipA, 2, 5);
        seed(ipB, 1, 5);

        mockMvc.perform(get(BASE_URL + "/{ip}", ipA).param("range", "1h").with(asAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ip").value(ipA))
                .andExpect(jsonPath("$.timestamps", hasSize(2)));
    }

    @Test
    void admin_byIp_emptyForUnknownIp() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{ip}", ipA).param("range", "1h").with(asAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ip").value(ipA))
                .andExpect(jsonPath("$.timestamps", hasSize(0)));
    }

    @Test
    void admin_byIp_invalidIp_returns400() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{ip}", "not-an-ip").with(asAdmin))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filter_logsRequestIp() throws Exception {
        mockMvc.perform(get("/api/account/keys").with(asRegularUser)
                        .with(req -> {
                            req.setRemoteAddr(filterIp);
                            return req;
                        }))
                .andExpect(status().isOk());

        boolean found = false;
        for (int i = 0; i < 20 && !found; i++) {
            Integer count = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM ip_hits WHERE ip = ?::inet AND time > now() - INTERVAL '1 minute'",
                    Integer.class, filterIp);
            found = count != null && count >= 1;
            if (!found) Thread.sleep(100);
        }
        if (!found) throw new AssertionError("IpHitFilter " + filterIp);

        mockMvc.perform(get(BASE_URL).param("ip", filterIp).param("range", "1h").with(asAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasKey(filterIp)))
                .andExpect(jsonPath("$['" + filterIp + "'].length()", greaterThanOrEqualTo(1)));
    }

    private void seed(String ip, int count, int minutesAgo) {
        for (int i = 0; i < count; i++) {
            jdbc.update(
                    "INSERT INTO ip_hits(time, ip) VALUES (now() - make_interval(mins => ?), ?::inet)",
                    minutesAgo, ip);
        }
    }
}