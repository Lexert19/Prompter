package com.example.promptengineering.service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.promptengineering.entity.ChatHistory;
import com.example.promptengineering.entity.ChatMessage;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.SaveMessage;
import com.example.promptengineering.repository.ChatHistoryRepository;
import com.example.promptengineering.repository.ChatMessageRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatHistoryService {
    @Autowired
    private ChatHistoryRepository chatHistoryRepository;
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public Mono<ChatHistory> createChatHistory(User user, String name) {
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setUserId(user.getEmail());
        chatHistory.setName(name);

        return chatHistoryRepository.save(chatHistory);
    }

    public Flux<ChatMessage> getChatMessages(User user, String chatHistoryId) {
        return chatHistoryRepository
                .findById(chatHistoryId)
                .flatMapMany(chatHistory -> {
                    if (user.getId().equals(chatHistory.getUserId())) {
                        return chatMessageRepository.findAllById(chatHistory.getMessages());

                    } else {
                        return Flux
                                .error(new IllegalArgumentException("User does not have access to this chat history."));
                    }
                })
                .switchIfEmpty(Flux.error(new NoSuchElementException("Chat history not found.")));

    }

    public Flux<ChatHistory> getHistory(User user) {
        return chatHistoryRepository
                .findAllByUserId(user.getId());
    }

    private Mono<ChatHistory> addIndexToChatHistory(User user, ChatMessage chatMessage) {
        return chatHistoryRepository
                .findById(chatMessage.getChatHistoryId())
                .flatMap(chatHistory -> {
                    if(chatHistory.getUserId().equals(user.getId())){
                        List<Long> messages = chatHistory.getMessages();
                        messages.add(chatMessage.getId());
                        chatHistory.setMessages(messages);
                        return chatHistoryRepository.save(chatHistory);
                    }else{
                        return Mono.error(new IllegalArgumentException("User is not the owner of this chat history."));
                    }
                   
                })
                .switchIfEmpty(Mono.error(new NoSuchElementException("Chat history not found.")));
    }

    public Mono<ChatMessage> saveMessage(User user, SaveMessage saveMessage) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setCache(saveMessage.isCache());
        chatMessage.setContent(saveMessage.getContent());
        chatMessage.setChatHistoryId(saveMessage.getId());
        chatMessage.setUserId(user.getId());

        return chatMessageRepository
        .save(chatMessage)
        .map(message ->{
           this.addIndexToChatHistory(user, message).subscribe();
           return message;
        });
    }
}
