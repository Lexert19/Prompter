package com.example.promptengineering.restController;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.Message;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.MessageBody;
import com.example.promptengineering.repository.ChatRepository;
import com.example.promptengineering.repository.MessageRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.HistoryService;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class HistoryControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    @Autowired
    private HistoryService historyService;

    private User user1;
    private User user2;
    private final String password = "password";

    @BeforeEach
    void setUp() {

        user1 = new User();
        user1.setEmail("user1-" + UUID.randomUUID() + "@example.com");
        user1.setPassword(passwordEncoder.encode(password));
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setEmail("user2-" + UUID.randomUUID() + "@example.com");
        user2.setPassword(passwordEncoder.encode(password));
        user2 = userRepository.save(user2);
    }

    @AfterEach
    void tearDown() {
        if (user1 != null && user1.getId() != null) {
            deleteChatsFor(user1);
            userRepository.delete(user1);
        }
        if (user2 != null && user2.getId() != null) {
            deleteChatsFor(user2);
            userRepository.delete(user2);
        }
    }

    private void deleteChatsFor(User owner) {
        List<Chat> chats = chatRepository.findByUser(owner);
        for (Chat c : chats) {
            messageRepository.deleteByChatId(c.getId());
        }
        chatRepository.deleteAll(chats);
    }

    private RequestPostProcessor asUser1() {
        return user(userService.loadUserByUsername(user1.getEmail()));
    }

    private RequestPostProcessor asUser2() {
        return user(userService.loadUserByUsername(user2.getEmail()));
    }

    @Test
    void getChats_shouldReturnAllUserChats() throws Exception {
        Chat chat1 = new Chat();
        chat1.setUser(user1);
        chat1.setCreatedAt(Instant.now());
        chat1 = chatRepository.save(chat1);

        Chat chat2 = new Chat();
        chat2.setUser(user1);
        chat2.setCreatedAt(Instant.now());
        chat2 = chatRepository.save(chat2);

        Chat chat3 = new Chat();
        chat3.setUser(user2);
        chat3.setCreatedAt(Instant.now());
        chatRepository.save(chat3);

        mockMvc.perform(get("/api/history/chats").with(asUser1()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2)).andExpect(
                        jsonPath("$.content[*].uuid", hasItems(chat1.getUuid().toString(),
                                chat2.getUuid().toString())));
    }

    @Test
    void unauthenticatedRequests_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/history/chats").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/history/chats")).andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/history/chats/1/messages"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/history/chats/1").with(csrf()))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/history/chats"))
                .andExpect(status().isUnauthorized());

        MessageBody body = new MessageBody();
        body.setChatUuid(UUID.randomUUID());
        body.setText("test");
        mockMvc.perform(post("/api/history/messages").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteAllChats_shouldRemoveOnlyOwnChats() throws Exception {
        Chat u1c1 = new Chat();
        u1c1.setUser(user1);
        u1c1.setCreatedAt(Instant.now());
        u1c1 = chatRepository.save(u1c1);
        Chat u1c2 = new Chat();
        u1c2.setUser(user1);
        u1c2.setCreatedAt(Instant.now());
        u1c2 = chatRepository.save(u1c2);
        Chat u2c1 = new Chat();
        u2c1.setUser(user2);
        u2c1.setCreatedAt(Instant.now());
        u2c1 = chatRepository.save(u2c1);

        Message m = new Message();
        m.setChat(u1c1);
        m.setUuid(UUID.randomUUID());
        m.setRole("user");
        m.setText("hello");
        m.setCreatedAt(Instant.now());
        messageRepository.save(m);

        mockMvc.perform(delete("/api/history/chats").with(csrf()).with(asUser1()))
                .andExpect(status().isNoContent());

        assertThat(chatRepository.findByUser(user1)).isEmpty();
        assertThat(messageRepository.count()).isZero();

        assertThat(chatRepository.findByUser(user2)).hasSize(1);
    }

    @Test
    void deleteAllChats_withoutAuthentication_shouldReturn401() throws Exception {
        mockMvc.perform(delete("/api/history/chats").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

}
