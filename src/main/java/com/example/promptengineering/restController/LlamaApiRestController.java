package com.example.promptengineering.restController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.ChatGPTSettings;
import com.example.promptengineering.service.LlamaApiService;
import com.fasterxml.jackson.core.JsonProcessingException;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/llama")
public class LlamaApiRestController {

     @Autowired
    private LlamaApiService llamaApiService;

    @PostMapping("/chat")
    public Flux<String> makeRequest(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            @RequestBody String chatgptBody) throws JsonProcessingException {

        User user = (User) oAuth2User;
        ChatGPTSettings chatGPTSettings = new ChatGPTSettings(user.getNvidiaKey());

        return llamaApiService.makeRequest(chatGPTSettings, chatgptBody);
    }
}
