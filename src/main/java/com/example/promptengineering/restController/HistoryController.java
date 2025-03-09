package com.example.promptengineering.restController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.Message;
import com.example.promptengineering.model.MessageBody;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.HistoryService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    @Autowired
    private HistoryService historyService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/chats")
    public Mono<ResponseEntity<Chat>> createChat(@AuthenticationPrincipal OAuth2User oAuth2User) {
        String userIdFromOAuth2 = oAuth2User.getAttribute("id").toString();
        return userRepository.findById(userIdFromOAuth2) 
                .switchIfEmpty(Mono.error(new RuntimeException("User not found for ID: " + userIdFromOAuth2))) 
                .flatMap(user -> historyService.createChat(user))
                .map(chat -> ResponseEntity.status(HttpStatus.CREATED).body(chat))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @PostMapping("/messages")
    public Mono<ResponseEntity<Message>> saveMessage(
            @RequestBody MessageBody messageBody,
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        String userIdFromOAuth2 = oAuth2User.getAttribute("id").toString(); 
        return userRepository.findById(userIdFromOAuth2) 
                .switchIfEmpty(Mono.error(new RuntimeException("User not found for ID: " + userIdFromOAuth2))) 
                .flatMap(user -> historyService.saveMessage(messageBody, user))
                .map(message -> ResponseEntity.status(HttpStatus.CREATED).body(message))
                .onErrorResume(IllegalArgumentException.class, e -> Mono.just(ResponseEntity.badRequest().build()))
                .onErrorResume(SecurityException.class, e -> Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).build()))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    @GetMapping("/chats/{chatId}/messages")
    public Mono<ResponseEntity<Flux<Message>>> getChatHistory(
            @PathVariable String chatId,
            @AuthenticationPrincipal OAuth2User oAuth2User) {
        String userIdFromOAuth2 = oAuth2User.getAttribute("id").toString();
        return userRepository.findById(userIdFromOAuth2) 
                .switchIfEmpty(Mono.error(new RuntimeException("User not found for ID: " + userIdFromOAuth2))) 
                .flatMap(user -> historyService.getChatHistory(chatId, user)
                        .collectList()
                        .map(messages -> ResponseEntity.ok(Flux.fromIterable(messages))))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }
}