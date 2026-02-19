package com.example.promptengineering.restController;

import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.Message;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.MessageBody;
import com.example.promptengineering.repository.ChatRepository;
import com.example.promptengineering.repository.MessageRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
public class HistoryControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

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

    private User user1;
    private User user2;
    private final String user1Email = "user1@example.com";
    private final String user2Email = "user2@example.com";
    private final String password = "password";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        messageRepository.deleteAll();
        chatRepository.deleteAll();
        userRepository.deleteAll();

        user1 = new User();
        user1.setEmail(user1Email);
        user1.setPassword(passwordEncoder.encode(password));
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setEmail(user2Email);
        user2.setPassword(passwordEncoder.encode(password));
        user2 = userRepository.save(user2);
    }

    private RequestPostProcessor asUser1() {
        return user(userService.loadUserByUsername(user1Email));
    }

    private RequestPostProcessor asUser2() {
        return user(userService.loadUserByUsername(user2Email));
    }

    @Test
    void createChat_shouldReturnCreatedChat() throws Exception {
        mockMvc.perform(post("/api/history/chats")
                        .with(asUser1()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.favorite").value(false))
                .andExpect(jsonPath("$.createdAt").exists());

        List<Chat> chats = chatRepository.findByUser(user1);
        assertThat(chats).hasSize(1);
        assertThat(chats.get(0).getUser().getId()).isEqualTo(user1.getId());
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

        mockMvc.perform(get("/api/history/chats")
                        .with(asUser1()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(chat1.getId()))
                .andExpect(jsonPath("$[1].id").value(chat2.getId()));
    }

    @Test
    void saveMessage_shouldPersistMessage() throws Exception {
        Chat chat = new Chat();
        chat.setUser(user1);
        chat.setCreatedAt(Instant.now());
        chat = chatRepository.save(chat);

        MessageBody messageBody = new MessageBody();
        messageBody.setChatId(chat.getId());
        messageBody.setText("Hello, world!");
        messageBody.setRole("user");
        messageBody.setStart(System.currentTimeMillis() - 1000);
        messageBody.setEnd(System.currentTimeMillis());
        messageBody.setCache(false);

        mockMvc.perform(post("/api/history/messages")
                        .with(asUser1())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("Hello, world!"))
                .andExpect(jsonPath("$.role").value("user"));

        List<Message> messages = messageRepository.findByChatId(chat.getId());
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).getText()).isEqualTo("Hello, world!");
    }

    @Test
    void getChatHistory_shouldReturnMessagesInOrder() throws Exception {
        Chat chat = new Chat();
        chat.setUser(user1);
        chat.setCreatedAt(Instant.now());
        chat = chatRepository.save(chat);

        Message msg1 = new Message();
        msg1.setChat(chat);
        msg1.setText("First");
        msg1.setRole("user");
        msg1.setCreatedAt(Instant.now().minusSeconds(10));
        messageRepository.save(msg1);

        Message msg2 = new Message();
        msg2.setChat(chat);
        msg2.setText("Second");
        msg2.setRole("assistant");
        msg2.setCreatedAt(Instant.now());
        messageRepository.save(msg2);

        mockMvc.perform(get("/api/history/chats/{chatId}/messages", chat.getId())
                        .with(asUser1()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].text").value("First"))
                .andExpect(jsonPath("$[1].text").value("Second"));
    }

    @Test
    void deleteChat_shouldRemoveChatAndMessages() throws Exception {
        Chat chat = new Chat();
        chat.setUser(user1);
        chat.setCreatedAt(Instant.now());
        chat = chatRepository.save(chat);

        Message msg = new Message();
        msg.setChat(chat);
        msg.setText("To be deleted");
        msg.setRole("user");
        msg.setCreatedAt(Instant.now());
        messageRepository.save(msg);

        mockMvc.perform(delete("/api/history/chats/{chatId}", chat.getId())
                        .with(asUser1()))
                .andExpect(status().isNoContent());

        Optional<Chat> deletedChat = chatRepository.findById(chat.getId());
        assertThat(deletedChat).isEmpty();
        List<Message> messages = messageRepository.findByChatId(chat.getId());
        assertThat(messages).isEmpty();
    }

    @Test
    void deleteChat_whenChatNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/history/chats/{chatId}", 999L)
                        .with(asUser1()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void saveMessage_whenChatNotFound_shouldReturn404() throws Exception {
        MessageBody messageBody = new MessageBody();
        messageBody.setChatId(999L);
        messageBody.setText("Test");
        messageBody.setRole("user");

        mockMvc.perform(post("/api/history/messages")
                        .with(asUser1())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageBody)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void accessAnotherUsersChat_shouldReturn403or404() throws Exception {
        Chat chat = new Chat();
        chat.setUser(user2);
        chat.setCreatedAt(Instant.now());
        chat = chatRepository.save(chat);

        mockMvc.perform(get("/api/history/chats/{chatId}/messages", chat.getId())
                        .with(asUser1()))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(delete("/api/history/chats/{chatId}", chat.getId())
                        .with(asUser1()))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void saveMessage_whenNotOwner_shouldReturn403() throws Exception {
        Chat chat = new Chat();
        chat.setUser(user2);
        chat.setCreatedAt(Instant.now());
        chat = chatRepository.save(chat);

        MessageBody messageBody = new MessageBody();
        messageBody.setChatId(chat.getId());
        messageBody.setText("Hack attempt");
        messageBody.setRole("user");

        mockMvc.perform(post("/api/history/messages")
                        .with(asUser1())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageBody)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void unauthenticatedRequests_shouldReturn401() throws Exception {
        mockMvc.perform(post("/api/history/chats"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/history/chats"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/history/chats/1/messages"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(delete("/api/history/chats/1"))
                .andExpect(status().isUnauthorized());

        MessageBody body = new MessageBody();
        body.setChatId(1L);
        body.setText("test");
        mockMvc.perform(post("/api/history/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void saveMessage_withAllFields_shouldStoreDocumentsAndImages() throws Exception {
        Chat chat = new Chat();
        chat.setUser(user1);
        chat.setCreatedAt(Instant.now());
        chat = chatRepository.save(chat);

        MessageBody messageBody = new MessageBody();
        messageBody.setChatId(chat.getId());
        messageBody.setText("Message with attachments");
        messageBody.setRole("user");
        messageBody.setDocuments(List.of("doc1.pdf", "doc2.docx"));
        messageBody.setImages(List.of("img1.png", "img2.jpg"));
        messageBody.setCache(true);
        messageBody.setStart(1000L);
        messageBody.setEnd(2000L);

        mockMvc.perform(post("/api/history/messages")
                        .with(asUser1())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documents[0]").value("doc1.pdf"))
                .andExpect(jsonPath("$.documents[1]").value("doc2.docx"))
                .andExpect(jsonPath("$.images[0]").value("img1.png"))
                .andExpect(jsonPath("$.images[1]").value("img2.jpg"))
                .andExpect(jsonPath("$.cache").value(true));

        Message saved = messageRepository.findByChatId(chat.getId()).get(0);
        assertThat(saved.getDocuments()).containsExactly("doc1.pdf", "doc2.docx");
        assertThat(saved.getImages()).containsExactly("img1.png", "img2.jpg");
        assertThat(saved.getCache()).isTrue();
        assertThat(saved.getStart()).isEqualTo(1000L);
        assertThat(saved.getEnd()).isEqualTo(2000L);
    }
}