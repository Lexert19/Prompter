package com.example.promptengineering.restController;

import com.example.promptengineering.entity.User;
import com.google.gson.Gson;
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
    private final Gson gson;

    public ClientRestController(ChatService chatService, Gson gson) {
        this.chatService = chatService;
        this.gson = gson;
    }

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@AuthenticationPrincipal User user,
                                              @RequestBody String body) {
        RequestBuilder request = gson.fromJson(body, RequestBuilder.class);
        return chatService.makeRequest(request, user);
    }

}
