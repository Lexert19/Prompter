package com.example.promptengineering.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.Message;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.MessageBody;
import com.example.promptengineering.repository.ChatRepository;
import com.example.promptengineering.repository.MessageRepository;

import reactor.core.publisher.Mono;

@Service
public class HistoryService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;


    public Chat createChat(User user) {
        Chat chat = new Chat();
        chat.setUserId(user.getId());
        chat.setCreatedAt(System.currentTimeMillis());
        chat.setFavorite(false);

        return chatRepository.save(chat);
    }

    public void deleteChat(String chatId, User user) {
        Optional<Chat> chat = chatRepository.findById(chatId);
        if (chat.isEmpty()) {
            throw new IllegalArgumentException("Chat not found with id: " + chatId);
        }
        checkUserAuthorization(chat.get(), user);
        chatRepository.delete(chat.get());
        messageRepository.deleteByChatId(chatId);
    }

    public Message saveMessage(MessageBody messageBody, User user) {
                Optional<Chat> chat = chatRepository.findById(messageBody.getChatId());
                if(chat.isEmpty())
                    throw new IllegalArgumentException("Chat not found with id: " + messageBody.getChatId());
                checkUserAuthorization(chat.get(),user);
                return convertAndSaveMessage(messageBody, chat.get());

    }

    private Chat checkUserAuthorization(Chat chat, User user) {
        if (!isUserAuthorizedForChat(chat, user)) {
            throw new SecurityException("User is not authorized to send messages to this chat.");
        }
        return chat;
    }

    private boolean isUserAuthorizedForChat(Chat chat, User user) {
        return chat.getUserId() != null && chat.getUserId().equals(user.getId());
    }

    public Message convertAndSaveMessage(MessageBody messageBody, Chat chat) {
        Message messageEntity = new Message();
        messageEntity.setChatId(messageBody.getChatId());
        messageEntity.setCreatedAt(System.currentTimeMillis());
        messageEntity.setStart(messageBody.getStart());
        messageEntity.setEnd(messageBody.getEnd());
        messageEntity.setText(messageBody.getText());
        messageEntity.setDocuments(messageBody.getDocuments());
        messageEntity.setImages(messageBody.getImages());
        messageEntity.setRole(messageBody.getRole());
        messageEntity.setCache(messageBody.getCache());

        return messageRepository.save(messageEntity);
    }

    public List<Message> getChatHistory(String chatId, User user) {
        Optional<Chat> chat = chatRepository.findById(chatId);
        return this.returnHistory(chat.orElse(null), user);
    }

    public List<Message> returnHistory(Chat chat, User user) {
        if (chat != null && user.getId().equals(chat.getUserId())) {
            return messageRepository.findByChatId(chat.getId());
        } else {
            return null;
        }
    }

    public List<Chat> getChatsForUser(User user) {
        return chatRepository.findByUserId(user.getId());
    }

}