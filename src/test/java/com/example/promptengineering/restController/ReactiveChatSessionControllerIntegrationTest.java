package com.example.promptengineering.restController;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.dto.SessionChatRequest;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.exception.ValidationException;
import com.example.promptengineering.model.RequestBuilder;
import com.example.promptengineering.repository.ChatRepository;
import com.example.promptengineering.repository.MessageRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.ChatService;
import com.example.promptengineering.service.ChatSession;
import com.example.promptengineering.service.ChatSessionRegistry;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReactiveChatSessionControllerIntegrationTest extends BaseIntegrationTest {
    private static final String BASE_URL = "/api/chat/session";

    @MockitoBean
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ChatSessionRegistry registry;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    private User user1;
    private User user2;
    private String user1Email;
    private String user2Email;
    private UUID chatUuid;
    private RequestPostProcessor asUser1;
    private RequestPostProcessor asUser2;

    @Value("${openrouter.api.key}")
    private String openRouterApiKey;
    private static final String MODEL = "~deepseek/deepseek-flash-latest";
    private static final String PROVIDER = "OPENROUTER";
    private static final String URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String SYSTEM_PROMPT = "You are a helpful assistant";

    @BeforeEach
    void setUp() throws ValidationException {
        user1Email = "session-" + UUID.randomUUID() + "@example.com";
        user2Email = "session-" + UUID.randomUUID() + "@example.com";

        user1 = createTestUser(user1Email);
        user2 = createTestUser(user2Email);

        chatUuid = UUID.randomUUID();
        asUser1 = user(userService.loadUserByUsername(user1Email));
        asUser2 = user(userService.loadUserByUsername(user2Email));
    }

    private SessionChatRequest requestWithTools(String text, List<String> tools) {
        return new SessionChatRequest(text, MODEL, PROVIDER, PROVIDER, URL, SYSTEM_PROMPT,
                tools, false);
    }

    private User createTestUser(String email) throws ValidationException {
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("password"));
        u = userRepository.save(u);
        userService.setUserKeys(u, Map.of(PROVIDER, openRouterApiKey));
        return userRepository.save(u);
    }

    private SessionChatRequest defaultRequest(String text) {
        return new SessionChatRequest(text, MODEL, PROVIDER, PROVIDER, URL, SYSTEM_PROMPT,
                null, false);
    }

    private void mockChatServiceWithTextResponse(String... chunks) {
        Flux<ServerSentEvent<String>> events = Flux.fromArray(chunks).index().map(t -> {
            long idx = t.getT1();
            String content = t.getT2();
            boolean last = idx == chunks.length - 1;
            String json = last
                    ? "{\"choices\":[{\"delta\":{\"content\":\"" + content
                            + "\"},\"finish_reason\":\"stop\"}]}"
                    : "{\"choices\":[{\"delta\":{\"content\":\"" + content + "\"}}]}";
            return ServerSentEvent.<String>builder().data(json).build();
        });
        when(chatService.makeRequest(any(RequestBuilder.class), any(User.class)))
                .thenReturn(events);
    }

    private MvcResult performChatBlocking(RequestPostProcessor auth,
                                          SessionChatRequest req)
            throws Exception {
        MvcResult result = mockMvc
                .perform(post(BASE_URL + "/{chatUuid}/chat", chatUuid).with(csrf())
                        .with(auth).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(request().asyncStarted()).andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
        return result;
    }

    @Test
    void state_whenNoSession_shouldReturn404() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{chatUuid}/state", chatUuid).with(asUser1))
                .andExpect(status().isNotFound());
    }

    @Test
    void state_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{chatUuid}/state", chatUuid))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void state_afterChat_shouldReturnSessionDetails() throws Exception {
        mockChatServiceWithTextResponse("Hi");
        performChatBlocking(asUser1, defaultRequest("Hi"));

        mockMvc.perform(get(BASE_URL + "/{chatUuid}/state", chatUuid).with(asUser1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chatUuid").value(chatUuid.toString()))
                .andExpect(jsonPath("$.model").value(MODEL))
                .andExpect(jsonPath("$.provider").value(PROVIDER))
                .andExpect(jsonPath("$.systemPrompt").value(SYSTEM_PROMPT))
                .andExpect(jsonPath("$.messageCount").value(2))
                .andExpect(jsonPath("$.hasToolCallsPending").value(false));
    }

    @Test
    void state_ofOtherUserSession_shouldReturn404() throws Exception {
        mockChatServiceWithTextResponse("Hi");
        performChatBlocking(asUser1, defaultRequest("Hi"));

        mockMvc.perform(get(BASE_URL + "/{chatUuid}/state", chatUuid).with(asUser2))
                .andExpect(status().isNotFound());
    }

    @Test
    void reset_whenNoSession_shouldReturnNoContent() throws Exception {
        mockMvc.perform(
                delete(BASE_URL + "/{chatUuid}", chatUuid).with(csrf()).with(asUser1))
                .andExpect(status().isNoContent());
    }

    @Test
    void reset_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{chatUuid}", chatUuid).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reset_afterChat_shouldRemoveSession() throws Exception {
        mockChatServiceWithTextResponse("Hi");
        performChatBlocking(asUser1, defaultRequest("Hi"));

        assertThat(registry.get(chatUuid, user1.getId())).isPresent();

        mockMvc.perform(
                delete(BASE_URL + "/{chatUuid}", chatUuid).with(csrf()).with(asUser1))
                .andExpect(status().isNoContent());

        assertThat(registry.get(chatUuid, user1.getId())).isEmpty();
    }

    @Test
    void reset_ofOtherUserSession_shouldNotAffectUser1() throws Exception {
        mockChatServiceWithTextResponse("Hi");
        performChatBlocking(asUser1, defaultRequest("Hi"));

        mockMvc.perform(
                delete(BASE_URL + "/{chatUuid}", chatUuid).with(csrf()).with(asUser2))
                .andExpect(status().isNoContent());

        assertThat(registry.get(chatUuid, user1.getId())).isPresent();
    }

    @Test
    void chat_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post(BASE_URL + "/{chatUuid}/chat", chatUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .content(objectMapper.writeValueAsString(defaultRequest("Hi"))))
                .andExpect(status().isUnauthorized());

        assertThat(registry.get(chatUuid, user1.getId())).isEmpty();
    }

    @Test
    void chat_shouldCreateSessionAndStreamTokensAndDone() throws Exception {
        mockChatServiceWithTextResponse("Hello", " world", "!");

        MvcResult result = performChatBlocking(asUser1, defaultRequest("Hello bot"));

        ChatSession session = registry.get(chatUuid, user1.getId()).orElseThrow();
        assertThat(session.getModel()).isEqualTo(MODEL);
        assertThat(session.getProvider()).isEqualTo(PROVIDER);
        assertThat(session.getUrl()).isEqualTo(URL);
        assertThat(session.getSystemPrompt()).isEqualTo(SYSTEM_PROMPT);
        assertThat(session.getMessages()).hasSize(2);
        assertThat(session.getMessages().get(0).getRole()).isEqualTo("user");
        assertThat(session.getMessages().get(1).getRole()).isEqualTo("assistant");

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("event:token", "event:done");
        assertThat(body).contains("\"finishReason\":\"stop\"");
    }

    @Test
    void chat_shouldPreserveConversationAcrossCalls() throws Exception {
        mockChatServiceWithTextResponse("Hi");
        performChatBlocking(asUser1, defaultRequest("First"));

        SessionChatRequest second = new SessionChatRequest("Second", null, null, null,
                null, null, null, null);
        performChatBlocking(asUser1, second);

        ChatSession session = registry.get(chatUuid, user1.getId()).orElseThrow();
        assertThat(session.getMessages()).hasSize(4);
        assertThat(session.getMessages().get(2).getRole()).isEqualTo("user");
        assertThat(session.getModel()).isEqualTo(MODEL);
        assertThat(session.getProvider()).isEqualTo(PROVIDER);
        assertThat(session.getSystemPrompt()).isEqualTo(SYSTEM_PROMPT);
    }

    @Test
    void chat_withMissingOpenRouterKey_shouldEmitErrorEvent() throws Exception {
        String noKeyEmail = "nokey-" + UUID.randomUUID() + "@example.com";
        User noKeyUser = new User();
        noKeyUser.setEmail(noKeyEmail);
        noKeyUser.setPassword(passwordEncoder.encode("password"));
        noKeyUser = userRepository.save(noKeyUser);

        try {
            UUID noKeyChat = UUID.randomUUID();
            RequestPostProcessor asNoKey = user(
                    userService.loadUserByUsername(noKeyEmail));

            MvcResult result = mockMvc
                    .perform(post(BASE_URL + "/{chatUuid}/chat", noKeyChat).with(csrf())
                            .with(asNoKey).contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .content(objectMapper
                                    .writeValueAsString(defaultRequest("Hi"))))
                    .andExpect(request().asyncStarted()).andReturn();

            mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());

            String body = result.getResponse().getContentAsString();
            assertThat(body).contains("event:error");
            assertThat(body).contains("Missing API key for OPENROUTER");
        } finally {
        }
    }

    @Test
    void chat_shouldExecuteToolCallAndContinueConversation() throws Exception {
        mockChatServiceWithToolCallThenText("call_test_1", "get_current_datetime",
                "{\"timezone\":\"UTC\"}", "The current UTC time is ", "right now.");

        MvcResult result = performChatBlocking(asUser1,
                requestWithTools(
                        "What is the current time in UTC? "
                                + "Use the get_current_datetime tool.",
                        List.of("get_current_datetime")));

        ChatSession session = registry.get(chatUuid, user1.getId()).orElseThrow();
        assertThat(session.getMessages()).hasSize(4);

        var userMsg = session.getMessages().get(0);
        assertThat(userMsg.getRole()).isEqualTo("user");

        var assistantWithCall = session.getMessages().get(1);
        assertThat(assistantWithCall.getRole()).isEqualTo("assistant");
        assertThat(assistantWithCall.getToolCalls()).hasSize(1);
        assertThat(assistantWithCall.getToolCalls().get(0).getName())
                .isEqualTo("get_current_datetime");
        assertThat(assistantWithCall.getToolCalls().get(0).getArguments())
                .contains("\"timezone\"").contains("UTC");

        var toolMsg = session.getMessages().get(2);
        assertThat(toolMsg.getRole()).isEqualTo("tool");
        assertThat(toolMsg.getToolCallId()).isEqualTo("call_test_1");
        assertThat(toolMsg.getStringContent())
                .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*");

        var finalAssistant = session.getMessages().get(3);
        assertThat(finalAssistant.getRole()).isEqualTo("assistant");
        assertThat(finalAssistant.getToolCalls()).isNullOrEmpty();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("event:tool_call");
        assertThat(body).contains("event:tool_result");
        assertThat(body).contains("event:token");
        assertThat(body).contains("event:done");
        assertThat(body).contains("get_current_datetime");
        assertThat(body).contains("\"finishReason\":\"stop\"");

        ArgumentCaptor<RequestBuilder> captor = ArgumentCaptor
                .forClass(RequestBuilder.class);
        verify(chatService, times(2)).makeRequest(captor.capture(), any(User.class));
        List<RequestBuilder> requests = captor.getAllValues();

        assertThat(requests.get(0).getTools()).isNotNull().isNotEmpty();
        assertThat(requests.get(0).getMessages()).hasSize(1);
        assertThat(requests.get(0).getMessages().get(0).getRole()).isEqualTo("user");

        assertThat(requests.get(1).getMessages()).hasSize(3);
        assertThat(requests.get(1).getMessages().get(1).getRole()).isEqualTo("assistant");
        assertThat(requests.get(1).getMessages().get(1).getToolCalls()).hasSize(1);
        assertThat(requests.get(1).getMessages().get(2).getRole()).isEqualTo("tool");
        assertThat(requests.get(1).getMessages().get(2).getToolCallId())
                .isEqualTo("call_test_1");
    }

    private void mockChatServiceWithToolCallThenText(String toolCallId, String toolName,
                                                     String toolArgs,
                                                     String... finalChunks)
            throws JsonProcessingException {

        String toolCallJson = buildToolCallJson(toolCallId, toolName, toolArgs);
        Flux<ServerSentEvent<String>> toolCallStream = Flux
                .just(ServerSentEvent.<String>builder().data(toolCallJson).build());

        Flux<ServerSentEvent<String>> textStream = Flux.fromArray(finalChunks).index()
                .map(t -> {
                    long idx = t.getT1();
                    String content = t.getT2();
                    boolean last = idx == finalChunks.length - 1;
                    String json = last
                            ? "{\"choices\":[{\"delta\":{\"content\":\"" + content
                                    + "\"},\"finish_reason\":\"stop\"}]}"
                            : "{\"choices\":[{\"delta\":{\"content\":\"" + content
                                    + "\"}}]}";
                    return ServerSentEvent.<String>builder().data(json).build();
                });

        when(chatService.makeRequest(any(RequestBuilder.class), any(User.class)))
                .thenReturn(toolCallStream).thenReturn(textStream);
    }

    private String buildToolCallJson(String id, String name, String args)
            throws JsonProcessingException {
        var toolCall = Map.of("index", 0, "id", id, "type", "function", "function",
                Map.of("name", name, "arguments", args));
        var choice = Map.of("delta", Map.of("tool_calls", List.of(toolCall)),
                "finish_reason", "tool_calls");
        return objectMapper.writeValueAsString(Map.of("choices", List.of(choice)));
    }
}
