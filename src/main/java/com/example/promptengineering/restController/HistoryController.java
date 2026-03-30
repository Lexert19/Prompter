package com.example.promptengineering.restController;

import com.example.promptengineering.dto.ChatDto;
import com.example.promptengineering.dto.MessageDto;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.exception.ResourceNotFoundException;
import com.example.promptengineering.exception.UserSecurityException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.Message;
import com.example.promptengineering.model.MessageBody;
import com.example.promptengineering.service.HistoryService;

import java.util.List;

@RestController
@RequestMapping("/api/history")
public class HistoryController {
    @Autowired
    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @PostMapping("/chats")
    public ResponseEntity<ChatDto> createChat(@AuthenticationPrincipal User user) {

        Chat chat = historyService.createChat(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ChatDto.fromEntity(chat));
    }

    @DeleteMapping("/chats/{chatId}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long chatId, @AuthenticationPrincipal User user)
            throws ResourceNotFoundException, UserSecurityException {
        historyService.deleteChat(chatId, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageDto> saveMessage(@RequestBody MessageBody messageBody,
            @AuthenticationPrincipal User user) throws UserSecurityException, ResourceNotFoundException {
        Message message = historyService.saveMessage(messageBody, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(MessageDto.fromEntity(message));

    }

    @GetMapping("/chats/{chatId}/messages")
    public ResponseEntity<List<MessageDto>> getChatHistory(@PathVariable Long chatId,
            @AuthenticationPrincipal User user) throws UserSecurityException, ResourceNotFoundException {
        List<Message> messages = historyService.getChatHistory(chatId, user);
        return ResponseEntity.ok(MessageDto.fromEntities(messages));
    }

    @GetMapping("/chats")
    public ResponseEntity<Page<ChatDto>> getChats(@AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

        Page<Chat> chatPage = historyService.getChatsForUser(user, page, size);
        Page<ChatDto> dtoPage = chatPage.map(ChatDto::fromEntity);
        return ResponseEntity.ok(dtoPage);
    }
}
