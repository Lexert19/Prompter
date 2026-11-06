package com.example.promptengineering.restController;

import com.example.promptengineering.BaseIntegrationTest;
import com.example.promptengineering.dto.SessionChatRequest;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeepSeekFunctionCallingE2ETest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${openrouter.api.key}")
    private String openRouterApiKey;

    @Test
    void deepSeek_shouldCallGetCurrentDatetimeTool() throws Exception {
        String email = "e2e-" + UUID.randomUUID() + "@example.com";
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("password"));
        user = userRepository.save(user);

        try {
            userService.setUserKeys(user, Map.of("OPENROUTER", openRouterApiKey));
            user = userRepository.save(user);

            RequestPostProcessor auth = user(userService.loadUserByUsername(email));
            UUID chatUuid = UUID.randomUUID();

            SessionChatRequest req = new SessionChatRequest(
                    "What is the current UTC time right now? "
                            + "You MUST call the get_current_datetime tool with timezone=UTC.",
                    "~deepseek/deepseek-flash-latest", "OPENROUTER", "OPENROUTER",
                    "https://openrouter.ai/api/v1/chat/completions",
                    "You are helpful. To answer any time-related question, "
                            + "you MUST use the get_current_datetime tool. "
                            + "Never guess the time.",
                    List.of("get_current_datetime"), false, List.of(), List.of(),
                    System.currentTimeMillis());

            MvcResult result = mockMvc
                    .perform(post("/api/chat/session/{uuid}/chat", chatUuid).with(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_EVENT_STREAM)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(request().asyncStarted()).andReturn();

            mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());

            String body = result.getResponse().getContentAsString();

            assertThat(body).as("DeepSeek must emit a tool_call for get_current_datetime")
                    .contains("event:tool_call").contains("get_current_datetime");

            assertThat(body).as("backend must execute the tool and return its result")
                    .contains("event:tool_result");

            assertThat(body).as("final assistant answer should come back")
                    .contains("event:token");

            assertThat(body).as("stream must end with done").contains("event:done");

        } finally {

        }
    }
}
