package com.example.promptengineering.restController;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.service.UserService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private UserService userService;

    @PostMapping("/save-key/{keyName}") 
    public Mono<String> saveKeyToMap(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            @PathVariable String keyName,
            @RequestBody String keyValue) {

        User user = (User) oAuth2User;

        return userService.saveKeyToMap(user, keyName, keyValue)
                .map(userSaved -> String.format("Key '%s' saved to map", keyName));
    }

    @GetMapping("/keys")
    public Mono<Map<String, String>> getAllKeys(
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        User user = (User) oAuth2User;
        return userService.getKeys(user);
    }
}
