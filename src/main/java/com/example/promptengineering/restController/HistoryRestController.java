package com.example.promptengineering.restController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.entity.ChatHistory;
import com.example.promptengineering.entity.ChatMessage;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.SaveMessage;
import com.example.promptengineering.service.ChatHistoryService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/history")
public class HistoryRestController {
    @Autowired
    private ChatHistoryService chatHistoryService;

    @PostMapping("/save")
    public Mono<Long> saveMessage(
            @AuthenticationPrincipal OAuth2User oAuth2user,
            @RequestBody SaveMessage saveMessage) {

        User user = (User) oAuth2user;

        return chatHistoryService
                .saveMessage(user, saveMessage)
                .map(chatHistory -> {
                    return chatHistory.getId();
                });

    }

    @GetMapping("/all")
    public Flux<ChatHistory> getHistory(@AuthenticationPrincipal OAuth2User oAuth2user){
        User user = (User) oAuth2user;

        return chatHistoryService.getHistory(user);
    }

    @GetMapping("/one")
    public Flux<ChatMessage> getChatHistory(
        @AuthenticationPrincipal OAuth2User oAuth2user,
        @RequestBody String chatId){
        User user = (User) oAuth2user;

        return chatHistoryService.getChatMessages(user, chatId);
    }

    @GetMapping("/get")
    public Flux<ChatMessage> getMethodName(
        @AuthenticationPrincipal OAuth2User oAuth2user,
        @RequestParam String chatId) {

            User user = (User) oAuth2user;
            return chatHistoryService.getChatMessages(user, chatId);
    }
    

    @PostMapping("/create")
    public Mono<String> createChat(
            @AuthenticationPrincipal OAuth2User oAuth2user,
            @RequestBody String name) {

        User user = (User) oAuth2user;

        return chatHistoryService
               .createChatHistory(user, name)
               .map(chatHistory ->{
                    return chatHistory.getId();
               });
    }
}
