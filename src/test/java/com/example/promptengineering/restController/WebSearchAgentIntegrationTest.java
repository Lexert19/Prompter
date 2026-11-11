package com.example.promptengineering.restController;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.dto.SessionChatRequest;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.RequestBuilder;
import com.example.promptengineering.repository.ChatRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.ChatService;
import com.example.promptengineering.service.ChatSession;
import com.example.promptengineering.service.ChatSessionRegistry;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebSearchAgentIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/chat/session";
    private static final String MODEL = "~deepseek/deepseek-flash-latest";
    private static final String PROVIDER = "OPENROUTER";
    private static final String URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String SYS = "You are a helpful assistant.";

    private static final String TAVILY_PROVIDER = "TAVILY";
    private static final String USER_TAVILY_KEY = "user-tavily-key";

    static WireMockServer searchApi;

    @BeforeAll
    static void startSearch() {
        searchApi = new WireMockServer(0);
        searchApi.start();
    }

    @AfterAll
    static void stopSearch() {
        searchApi.stop();
    }

    @DynamicPropertySource
    static void searchProps(DynamicPropertyRegistry r) {
        r.add("tools.search.base-url", () -> "http://localhost:" + searchApi.port());
    }

    @BeforeEach
    void reset() {
        searchApi.resetAll();
    }

    @MockitoBean
    private ChatService chatService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ChatSessionRegistry registry;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${openrouter.api.key}")
    private String openRouterApiKey;

    private User user1;
    private UUID chatUuid;
    private RequestPostProcessor asUser1;

    @BeforeEach
    void setUp() throws Exception {
        String email = "ws-" + UUID.randomUUID() + "@example.com";
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("password"));
        u = userRepository.save(u);
        userService.setUserKeys(u,
                Map.of(PROVIDER, openRouterApiKey, TAVILY_PROVIDER, USER_TAVILY_KEY));
        user1 = userRepository.save(u);

        chatUuid = UUID.randomUUID();
        asUser1 = user(userService.loadUserByUsername(email));
    }

    private SessionChatRequest requestWithTools(String text, List<String> tools) {
        return new SessionChatRequest(text, MODEL, PROVIDER, PROVIDER, URL, SYS, tools,
                false);
    }

    private MvcResult performChatBlocking(SessionChatRequest req) throws Exception {
        MvcResult result = mockMvc
                .perform(post(BASE_URL + "/{chatUuid}/chat", chatUuid).with(csrf())
                        .with(asUser1).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(request().asyncStarted()).andReturn();
        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
        return result;
    }

    private void mockToolCallThenText(String toolCallId, String toolName, String args,
                                      String... chunks)
            throws Exception {
        var toolCall = Map.of("index", 0, "id", toolCallId, "type", "function",
                "function", Map.of("name", toolName, "arguments", args));
        var choice = Map.of("delta", Map.of("tool_calls", List.of(toolCall)),
                "finish_reason", "tool_calls");
        Flux<ServerSentEvent<String>> toolStream = Flux.just(ServerSentEvent
                .<String>builder()
                .data(objectMapper.writeValueAsString(Map.of("choices", List.of(choice))))
                .build());

        Flux<ServerSentEvent<String>> textStream = Flux.fromArray(chunks).index()
                .map(t -> {
                    long idx = t.getT1();
                    boolean last = idx == chunks.length - 1;
                    String json = last
                            ? "{\"choices\":[{\"delta\":{\"content\":\"" + t.getT2()
                                    + "\"},\"finish_reason\":\"stop\"}]}"
                            : "{\"choices\":[{\"delta\":{\"content\":\"" + t.getT2()
                                    + "\"}}]}";
                    return ServerSentEvent.<String>builder().data(json).build();
                });

        when(chatService.makeRequest(any(RequestBuilder.class), any(User.class)))
                .thenReturn(toolStream).thenReturn(textStream);
    }

    @Test
    void webSearch_executesAndFeedsResultsBackToModel() throws Exception {
        searchApi
                .stubFor(WireMock.post(WireMock.urlEqualTo("/search")).willReturn(WireMock
                        .okJson("""
                                {"results":[
                                  {"title":"Spring Boot 3.2 released","url":"https://spring.io/blog/3-2",
                                   "content":"Spring Boot 3.2 brings virtual thread support."}
                                ]}
                                """)));

        mockToolCallThenText("call_ws_1", "web_search", "{\"query\":\"spring boot 3.2\"}",
                "Spring Boot 3.2 was released on ", "November 23, 2023.");

        MvcResult result = performChatBlocking(requestWithTools(
                "When was Spring Boot 3.2 released?", List.of("web_search")));

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("event:tool_call").contains("event:tool_result")
                .contains("event:token").contains("event:done")
                .contains("\"finishReason\":\"stop\"").contains("web_search");

        searchApi.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/search"))
                .withHeader("Authorization",
                        WireMock.equalTo("Bearer " + USER_TAVILY_KEY))
                .withRequestBody(WireMock.matchingJsonPath("$.query",
                        WireMock.equalTo("spring boot 3.2"))));

        ChatSession session = registry.get(chatUuid, user1.getId()).orElseThrow();
        assertThat(session.getMessages()).hasSize(4);

        assertThat(session.getMessages().get(0).getRole()).isEqualTo("user");

        var assistantWithCall = session.getMessages().get(1);
        assertThat(assistantWithCall.getRole()).isEqualTo("assistant");
        assertThat(assistantWithCall.getToolCalls()).hasSize(1);
        assertThat(assistantWithCall.getToolCalls().get(0).getName())
                .isEqualTo("web_search");

        var toolMsg = session.getMessages().get(2);
        assertThat(toolMsg.getRole()).isEqualTo("tool");
        assertThat(toolMsg.getToolCallId()).isEqualTo("call_ws_1");
        assertThat(toolMsg.getStringContent()).contains("https://spring.io/blog/3-2")
                .contains("Spring Boot 3.2 released");

        var finalAssistant = session.getMessages().get(3);
        assertThat(finalAssistant.getRole()).isEqualTo("assistant");
        assertThat(finalAssistant.getToolCalls()).isNullOrEmpty();
    }

    @Test
    void webSearch_isNotCalledWhenToolDisabled() throws Exception {
        mockToolCallThenText("call_ws_ignored", "web_search", "{\"query\":\"x\"}",
                "I can't search right now.");

        performChatBlocking(requestWithTools("search x", null));

        searchApi.verify(0, postRequestedFor(urlEqualTo("/search")));
    }

    @Test
    void webSearch_providerErrorIsEmittedAsToolError() throws Exception {
        searchApi.stubFor(WireMock.post(WireMock.urlEqualTo("/search")).willReturn(
                WireMock.aResponse().withStatus(500).withBody("upstream down")));

        mockToolCallThenText("call_ws_err", "web_search", "{\"query\":\"x\"}",
                "Sorry, the search failed.");

        MvcResult result = performChatBlocking(
                requestWithTools("search x", List.of("web_search")));

        assertThat(result.getResponse().getContentAsString()).contains("event:tool_error")
                .contains("web_search");

        ChatSession session = registry.get(chatUuid, user1.getId()).orElseThrow();
        assertThat(session.getMessages()).hasSize(4);
        var toolMsg = session.getMessages().get(2);
        assertThat(toolMsg.getRole()).isEqualTo("tool");
        assertThat(toolMsg.getStringContent()).contains("web_search").contains("failed");
    }

    @Test
    void webSearch_rejectsInvalidJsonArguments() throws Exception {
        mockToolCallThenText("call_ws_bad", "web_search", "{not-json", "Tool failed.");

        MvcResult result = performChatBlocking(
                requestWithTools("search", List.of("web_search")));

        assertThat(result.getResponse().getContentAsString()).contains("event:tool_error")
                .contains("Invalid tool arguments");

        searchApi.verify(0, postRequestedFor(urlEqualTo("/search")));
    }

    @Test
    void webSearch_returnsToolErrorWhenUserHasNoTavilyKey() throws Exception {
        String email = "ws-nokey-" + UUID.randomUUID() + "@example.com";
        User u = new User();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode("password"));
        u = userRepository.save(u);
        userService.setUserKeys(u, Map.of(PROVIDER, openRouterApiKey));
        userRepository.save(u);
        RequestPostProcessor asUserNoKey = user(userService.loadUserByUsername(email));

        mockToolCallThenText("call_ws_nokey", "web_search", "{\"query\":\"x\"}",
                "Sorry, I can't search right now.");

        MvcResult result = mockMvc
                .perform(post(BASE_URL + "/{chatUuid}/chat", chatUuid).with(csrf())
                        .with(asUserNoKey).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .content(objectMapper.writeValueAsString(
                                requestWithTools("search x", List.of("web_search")))))
                .andExpect(request().asyncStarted()).andReturn();
        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("event:tool_error").contains("TAVILY");

        searchApi.verify(0, postRequestedFor(urlEqualTo("/search")));
    }

    @Test
    void webSearch_usesCallerKeyNotOtherUsersKey() throws Exception {
        String otherEmail = "ws-other-" + UUID.randomUUID() + "@example.com";
        User other = new User();
        other.setEmail(otherEmail);
        other.setPassword(passwordEncoder.encode("password"));
        other = userRepository.save(other);
        userService.setUserKeys(other, Map.of(PROVIDER, openRouterApiKey, TAVILY_PROVIDER,
                "other-user-tavily-key"));
        userRepository.save(other);
        RequestPostProcessor asOther = user(userService.loadUserByUsername(otherEmail));

        searchApi.stubFor(WireMock.post(WireMock.urlEqualTo("/search"))
                .willReturn(WireMock.okJson("{\"results\":[]}")));

        mockToolCallThenText("call_ws_iso", "web_search", "{\"query\":\"x\"}", "ok.");

        UUID otherChat = UUID.randomUUID();
        MvcResult result = mockMvc
                .perform(post(BASE_URL + "/{chatUuid}/chat", otherChat).with(csrf())
                        .with(asOther).contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .content(objectMapper.writeValueAsString(
                                requestWithTools("search x", List.of("web_search")))))
                .andExpect(request().asyncStarted()).andReturn();
        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());

        searchApi.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/search"))
                .withHeader("Authorization",
                        WireMock.equalTo("Bearer other-user-tavily-key")));
    }
}
