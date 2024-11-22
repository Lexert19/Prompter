package com.example.promptengineering.restController;

import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.ClaudeBody;
import com.example.promptengineering.model.ClaudeSettings;
import com.example.promptengineering.service.ClaudeApiService;
import com.fasterxml.jackson.core.JsonProcessingException;

import reactor.core.publisher.Flux;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/claude")
public class ClaudeApiRestController {
    @Autowired
    private ClaudeApiService claudeApiService;

    @PostMapping("/chat")
    public Flux<String> makeRequest(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            @RequestBody ClaudeBody claudeBody) throws JsonProcessingException {

        User user = (User) oAuth2User;
        ClaudeSettings claudeSettings = new ClaudeSettings(user.getClaudeKey());

        return claudeApiService.makeRequest(claudeSettings, claudeBody);
    }


}
