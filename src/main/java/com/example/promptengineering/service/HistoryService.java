package com.example.promptengineering.service;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.Message;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.MessageBody;
import com.example.promptengineering.repository.ChatRepository;
import com.example.promptengineering.repository.MessageRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class HistoryService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

   
    public Mono<Chat> createChat(User user) { 
        Chat chat = new Chat(); 
        chat.setUserId(user.getId());
        chat.setCreatedAt(LocalDate.now()); 
        chat.setFavorite(false);

        return chatRepository.save(chat);
    }

    public Mono<Message> saveMessage(MessageBody messageBody, User user) { 
        return chatRepository.findById(messageBody.getChatId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Chat not found with id: " + messageBody.getChatId()))) 
                .flatMap(chat -> checkUserAuthorization(chat, user)) 
                .flatMap(chat -> convertAndSaveMessage(messageBody, chat)); 
    }

    private Mono<Chat> checkUserAuthorization(Chat chat, User user) {
        if (!isUserAuthorizedForChat(chat, user)) { 
            return Mono.error(new SecurityException("User is not authorized to send messages to this chat."));
        }
        return Mono.just(chat); 
    }

    private boolean isUserAuthorizedForChat(Chat chat, User user) {
        return chat.getUserId() != null && chat.getUserId().equals(user.getId());
    }

    public Mono<Message> convertAndSaveMessage(MessageBody messageBody, Chat chat) {
        Message messageEntity = new Message();
        messageEntity.setChatId(messageBody.getChatId());
        messageEntity.setCreatedAt(LocalDate.now());
        messageEntity.setDuration(messageBody.getDuration());
        messageEntity.setText(messageBody.getText());
        messageEntity.setDocuments(messageBody.getDocuments());
        messageEntity.setImages(messageBody.getImages());
        messageEntity.setRole(messageBody.getRole()); 
        messageEntity.setCache(messageBody.getCache());
        
        return messageRepository.save(messageEntity);
    }

    public Flux<Message> getChatHistory(String chatId, User user) {
        return chatRepository.findById(chatId) 
        .flatMapMany(chat -> this.returnHistory(chat, user));
    }

    public Flux<Message> returnHistory(Chat chat, User user){
        if (chat != null && user.getId().equals(chat.getUserId())) { 
            return messageRepository.findByChatId(chat.getId()); 
        } else {
            return Flux.empty(); 
        }
    }

    public Flux<Chat> getChatsForUser(User user) {
        return chatRepository.findByUserId(user.getId());
    }
}