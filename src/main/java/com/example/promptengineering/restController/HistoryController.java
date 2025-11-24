package com.example.promptengineering.restController;

import com.example.promptengineering.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.Message;
import com.example.promptengineering.model.MessageBody;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.HistoryService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    @Autowired
    private HistoryService historyService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/chats")
    public ResponseEntity<Chat> createChat(
                                           @AuthenticationPrincipal User user) {

        Chat chat = historyService.createChat(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(chat);
    }

    @DeleteMapping("/chats/{chatId}")
    public ResponseEntity<Void> deleteChat(
            @PathVariable Long chatId,
            @AuthenticationPrincipal User user) {
        historyService.deleteChat(chatId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/messages")
    public ResponseEntity<Message> saveMessage(
            @RequestBody MessageBody messageBody,
            @AuthenticationPrincipal User user) {
        Message message = historyService.saveMessage(messageBody, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);

    }

    @GetMapping("/chats/{chatId}/messages")
    public ResponseEntity<List<Message>> getChatHistory(
            @PathVariable Long chatId,
            @AuthenticationPrincipal User user) {
        List<Message> messages = historyService.getChatHistory(chatId, user);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/chats")
    public ResponseEntity<List<Chat>> getChats(@AuthenticationPrincipal User user) {
        List<Chat> chats = historyService.getChatsForUser(user);
        return ResponseEntity.ok(chats);
    }
}