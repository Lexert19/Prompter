package com.example.promptengineering.restController;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.dto.PromptDto;
import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.Message;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.ChatRepository;
import com.example.promptengineering.repository.MessageRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatPromptsEndpointIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/chat/session";
    private static final int MAX_LEN = 256;

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
    private RequestPostProcessor asUser1;
    private RequestPostProcessor asUser2;

    @BeforeEach
    void setUp() {
        user1 = createUser("prompts-u1-" + UUID.randomUUID() + "@example.com");
        user2 = createUser("prompts-u2-" + UUID.randomUUID() + "@example.com");
        asUser1 = user(userService.loadUserByUsername(user1.getEmail()));
        asUser2 = user(userService.loadUserByUsername(user2.getEmail()));
    }

    private User createUser(String email) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("password"));
        return userRepository.save(u);
    }

    private Chat createChat(User owner) {
        Chat c = new Chat();
        c.setUser(owner);
        c.setCreatedAt(Instant.now());
        return chatRepository.save(c);
    }

    private Message createMessage(Chat chat, String role, String text,
                                  Instant createdAt) {
        Message m = new Message();
        m.setChat(chat);
        m.setUuid(UUID.randomUUID());
        m.setRole(role);
        m.setText(text);
        m.setCreatedAt(createdAt);
        m.setStart(createdAt.toEpochMilli());
        m.setEnd(createdAt.toEpochMilli());
        return messageRepository.save(m);
    }

    private List<PromptDto> readBody(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<List<PromptDto>>() {
                });
    }

    @Test
    void returnsOnlyUserPromptsForGivenChat() throws Exception {
        Chat chat = createChat(user1);
        Instant t0 = Instant.now();

        createMessage(chat, "user", "first prompt", t0);
        createMessage(chat, "assistant", "assistant reply", t0.plusMillis(1));
        createMessage(chat, "user", "second prompt", t0.plusMillis(2));
        createMessage(chat, "assistant", "another reply", t0.plusMillis(3));
        Message tool = new Message();
        tool.setChat(chat);
        tool.setUuid(UUID.randomUUID());
        tool.setRole("tool");
        tool.setToolName("get_current_datetime");
        tool.setStringContent("2025-01-01T00:00:00Z");
        tool.setCreatedAt(t0.plusMillis(4));
        tool.setStart(t0.plusMillis(4).toEpochMilli());
        messageRepository.save(tool);

        MvcResult result = mockMvc
                .perform(get(BASE_URL + "/{chatUuid}/prompts", chat.getUuid())
                        .with(asUser1))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2))
                .andReturn();

        List<PromptDto> prompts = readBody(result);
        assertThat(prompts).extracting(PromptDto::text).containsExactly("first prompt",
                "second prompt");
        assertThat(prompts).allMatch(p -> !p.truncated());
    }

    @Test
    void returnsPromptsOrderedByCreatedAtAsc() throws Exception {
        Chat chat = createChat(user1);
        Instant base = Instant.now().minusSeconds(100);

        createMessage(chat, "user", "third", base.plusSeconds(30));
        createMessage(chat, "user", "first", base);
        createMessage(chat, "user", "second", base.plusSeconds(10));

        MvcResult result = mockMvc.perform(
                get(BASE_URL + "/{chatUuid}/prompts", chat.getUuid()).with(asUser1))
                .andExpect(status().isOk()).andReturn();

        assertThat(readBody(result)).extracting(PromptDto::text).containsExactly("first",
                "second", "third");
    }

    @Test
    void truncatesLongPromptTo256CodePoints() throws Exception {
        Chat chat = createChat(user1);
        String raw = "a".repeat(300);
        createMessage(chat, "user", raw, Instant.now());

        MvcResult result = mockMvc.perform(
                get(BASE_URL + "/{chatUuid}/prompts", chat.getUuid()).with(asUser1))
                .andExpect(status().isOk()).andReturn();

        PromptDto p = readBody(result).get(0);
        assertThat(p.text()).hasSize(MAX_LEN);
        assertThat(p.text()).isEqualTo("a".repeat(MAX_LEN));
        assertThat(p.truncated()).isTrue();
    }

    @Test
    void doesNotTruncatePromptAtExactly256CodePoints() throws Exception {
        Chat chat = createChat(user1);
        String raw = "x".repeat(MAX_LEN);
        createMessage(chat, "user", raw, Instant.now());

        MvcResult result = mockMvc.perform(
                get(BASE_URL + "/{chatUuid}/prompts", chat.getUuid()).with(asUser1))
                .andExpect(status().isOk()).andReturn();

        PromptDto p = readBody(result).get(0);
        assertThat(p.text()).isEqualTo(raw);
        assertThat(p.truncated()).isFalse();
    }

    @Test
    void doesNotBreakSurrogatePairsWhenTruncating() throws Exception {
        Chat chat = createChat(user1);
        String raw = "😀".repeat(300);
        createMessage(chat, "user", raw, Instant.now());

        MvcResult result = mockMvc.perform(
                get(BASE_URL + "/{chatUuid}/prompts", chat.getUuid()).with(asUser1))
                .andExpect(status().isOk()).andReturn();

        PromptDto p = readBody(result).get(0);

        assertThat(p.truncated()).isTrue();
        assertThat(p.text().codePointCount(0, p.text().length())).isEqualTo(MAX_LEN);
        assertThat(p.text().length()).isEqualTo(MAX_LEN * 2);
        char last = p.text().charAt(p.text().length() - 1);
        assertThat(Character.isHighSurrogate(last)).isFalse();
        assertThat(Character.isLowSurrogate(last)).isTrue();
    }

    @Test
    void returns404WhenChatBelongsToAnotherUser() throws Exception {
        Chat chatOfUser2 = createChat(user2);
        createMessage(chatOfUser2, "user", "user2 secret prompt", Instant.now());

        mockMvc.perform(get(BASE_URL + "/{chatUuid}/prompts", chatOfUser2.getUuid())
                .with(asUser1)).andExpect(status().isNotFound());

        mockMvc.perform(get(BASE_URL + "/{chatUuid}/prompts", chatOfUser2.getUuid())
                .with(asUser2)).andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void returns404ForNonExistentChat() throws Exception {
        mockMvc.perform(
                get(BASE_URL + "/{chatUuid}/prompts", UUID.randomUUID()).with(asUser1))
                .andExpect(status().isNotFound());
    }

    @Test
    void doesNotCreateChatAsSideEffectForNonExistentUuid() throws Exception {
        long before = chatRepository.findByUser(user1).size();
        UUID randomUuid = UUID.randomUUID();

        mockMvc.perform(get(BASE_URL + "/{chatUuid}/prompts", randomUuid).with(asUser1))
                .andExpect(status().isNotFound());

        long after = chatRepository.findByUser(user1).size();
        assertThat(after).as("GET must not create a chat (ensureChat regression)")
                .isEqualTo(before);
        assertThat(chatRepository.findByUuid(randomUuid)).isEmpty();
    }

    @Test
    void returns401WithoutAuthentication() throws Exception {
        Chat chat = createChat(user1);
        createMessage(chat, "user", "x", Instant.now());

        mockMvc.perform(get(BASE_URL + "/{chatUuid}/prompts", chat.getUuid()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returnsEmptyListWhenChatHasNoUserMessages() throws Exception {
        Chat chat = createChat(user1);
        createMessage(chat, "assistant", "assistant only", Instant.now());

        mockMvc.perform(
                get(BASE_URL + "/{chatUuid}/prompts", chat.getUuid()).with(asUser1))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void promptsAreScopedToSingleChat() throws Exception {
        Chat chatA = createChat(user1);
        Chat chatB = createChat(user1);
        createMessage(chatA, "user", "prompt A", Instant.now());
        createMessage(chatB, "user", "prompt B", Instant.now());

        MvcResult resultA = mockMvc.perform(
                get(BASE_URL + "/{chatUuid}/prompts", chatA.getUuid()).with(asUser1))
                .andExpect(status().isOk()).andReturn();

        assertThat(readBody(resultA)).extracting(PromptDto::text)
                .containsExactly("prompt A");
    }
}
