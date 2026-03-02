package com.example.promptengineering.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Value("${app.chat.page.max-size}")
    private int maxChatPageSize;

    @Value("${app.message.max-total-size:10485760}")
    private long maxTotalMessageSize;

    @Value("${app.message.max-images:10}")
    private int maxImages;

    @Value("${app.message.max-documents:100}")
    private int maxDocuments;



    public Chat createChat(User user) {
        Chat chat = new Chat();
        chat.setUser(user);
        chat.setCreatedAt(Instant.now());
        chat.setFavorite(false);

        return chatRepository.save(chat);
    }

    public void deleteChat(Long chatId, User user) {
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
        return chat.getUser() != null && chat.getUser().equals(user);
    }

    public Message convertAndSaveMessage(MessageBody messageBody, Chat chat) {
        if (messageBody.getImages() != null && messageBody.getImages().size() > maxImages) {
            throw new IllegalArgumentException("Too many images. Max allowed: " + maxImages);
        }

        if (messageBody.getDocuments() != null && messageBody.getDocuments().size() > maxDocuments) {
            throw new IllegalArgumentException("Too many documents. Max allowed: " + maxDocuments);
        }

        Message messageEntity = new Message();
        messageEntity.setChat(chat);
        messageEntity.setCreatedAt(Instant.now());
        messageEntity.setStart(messageBody.getStart());
        messageEntity.setEnd(messageBody.getEnd());
        messageEntity.setText(messageBody.getText());
        messageEntity.setDocuments(messageBody.getDocuments());
        messageEntity.setImages(messageBody.getImages());
        messageEntity.setRole(messageBody.getRole());
        messageEntity.setCache(messageBody.getCache());

        return messageRepository.save(messageEntity);
    }

    public List<Message> getChatHistory(Long chatId, User user) throws Exception {
        Optional<Chat> chat = chatRepository.findById(chatId);
        if(chat.isEmpty()){
            throw new Exception();
        }
        if(!chat.get().getUser().equals(user)){
            throw new Exception();
        }
        List<Message> messages = messageRepository.findByChatId(chatId);
        long totalSize = messages.stream()
                .mapToLong(m -> {
                    long size = m.getText() != null ? m.getText().length() : 0;
                    if (m.getDocuments() != null) {
                        size += m.getDocuments().stream()
                                .mapToLong(d -> d != null ? d.length() : 0)
                                .sum();
                    }
                    if (m.getImages() != null) {
                        size += m.getImages().stream()
                                .mapToLong(i -> i != null ? i.length() : 0)
                                .sum();
                    }
                    return size;
                })
                .sum();
        if (totalSize > maxTotalMessageSize) {
            throw new Exception("Total message size (text + documents + images) too large: " + totalSize + " characters, max allowed: " + maxTotalMessageSize);
        }
        return messages;
    }



    public Page<Chat> getChatsForUser(User user, int page, int size) {
        if (size > maxChatPageSize) {
            size = maxChatPageSize;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return chatRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

}