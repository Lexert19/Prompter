package com.example.promptengineering.restController;

import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.service.UserService;

import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private UserService userService;

    @PostMapping("/claude-key")
    public Mono<String> setClaudeKey(
        @AuthenticationPrincipal OAuth2User oAuth2User,    
        @RequestBody String key) {
        
        User user = (User) oAuth2User;
        
        return userService.setClaudeKey(key, user)
            .map(userSaved -> {
                return "Saved";
            });
    }

    @PostMapping("/chatgpt-key")
    public Mono<String> setChatgptKey(
        @AuthenticationPrincipal OAuth2User oAuth2User,    
        @RequestBody String key) {
        
        User user = (User) oAuth2User;
        
        return userService.setChatgptKey(key, user)
            .map(userSaved -> "ChatGPT Key Saved");
    }

    @PostMapping("/nvidia-key")
    public Mono<String> setNvidiaKey(
        @AuthenticationPrincipal OAuth2User oAuth2User,    
        @RequestBody String key) {
        
        User user = (User) oAuth2User;
        
        return userService.setNvidiaKey(key, user)
            .map(userSaved -> "NVIDIA Key Saved");
    }


    
}
