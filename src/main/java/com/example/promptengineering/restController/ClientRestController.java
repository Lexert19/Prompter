package com.example.promptengineering.restController;

import com.example.promptengineering.entity.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.model.RequestBuilder;
import com.example.promptengineering.service.ChatService;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/client")
public class ClientRestController {
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    public ClientRestController(ChatService chatService, ObjectMapper objectMapper) {
        this.chatService = chatService;
      this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@AuthenticationPrincipal User user,
                                              @RequestBody String body)
        throws JsonProcessingException {
        RequestBuilder request = objectMapper.readValue(body, RequestBuilder.class);
        return chatService.makeRequest(request, user);
    }

}
