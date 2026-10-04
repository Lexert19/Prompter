package com.example.promptengineering.service;

import com.example.promptengineering.exception.ValidationException;
import java.time.Instant;
import java.util.List;

import com.example.promptengineering.exception.ResourceNotFoundException;
import com.example.promptengineering.exception.UserSecurityException;
import jakarta.transaction.Transactional;
import java.util.UUID;
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

@Service
public class HistoryService {

    private final MessageRepository messageRepository;

    private final ChatRepository chatRepository;

    @Value("${app.chat.page.max-size}")
    private int maxChatPageSize;

    @Value("${app.message.max-images:10}")
    private int maxImages;

    @Value("${app.message.max-documents:100}")
    private int maxDocuments;

    @Value("${app.message.max-per-chat:200}")
    private int maxMessagesPerChat;

    @Value("${app.message.max-total-size:10485760}")
    private long maxTotalMessageSize;

    public HistoryService(MessageRepository messageRepository,
            ChatRepository chatRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
    }

    public Chat createChat(User user) {
        Chat chat = new Chat();
        chat.setUser(user);
        chat.setCreatedAt(Instant.now());
        chat.setFavorite(false);

        return chatRepository.save(chat);
    }

    @Transactional
    public void deleteChat(UUID chatUuid, User user) {
        Chat chat = chatRepository.findByUuid(chatUuid).orElseThrow(
                () -> new ResourceNotFoundException("Chat not found: " + chatUuid));
        if (isUserAuthorizedForChat(chat, user)) {
            messageRepository.deleteByChatId(chat.getId());
            chatRepository.delete(chat);
        } else {
            throw new UserSecurityException(
                    "User is not authorized to send messages to this chat.");
        }
    }

    public Message saveMessage(MessageBody messageBody, User user)
            throws ValidationException {
        UUID chatUuid = messageBody.getChatUuid();
        Chat chat = chatRepository.findByUuid(chatUuid).orElseThrow(
                () -> new ResourceNotFoundException("Chat not found: " + chatUuid));
        if (isUserAuthorizedForChat(chat, user)) {
            return convertAndSaveMessage(messageBody, chat);
        } else {
            throw new UserSecurityException(
                    "User is not authorized to send messages to this chat.");
        }
    }

    private Chat getAuthorizedChat(UUID chatUuid, User user) {
        return chatRepository.findByUuidAndUser(chatUuid, user).orElseThrow(
                () -> new ResourceNotFoundException("Chat not found: " + chatUuid));
    }

    private boolean isUserAuthorizedForChat(Chat chat, User user) {
        return chat.getUser() != null && chat.getUser().getId().equals(user.getId());
    }

    private Message convertAndSaveMessage(MessageBody messageBody, Chat chat)
            throws ValidationException {
        long newSize = calculateMessageSize(messageBody);

        if (chat.getTotalSize() + newSize > maxTotalMessageSize) {
            throw new ValidationException("Chat size limit reached. Max: "
                    + maxTotalMessageSize + ", current: " + chat.getTotalSize());
        }

        if (messageBody.getImages() != null
                && messageBody.getImages().size() > maxImages) {
            throw new ValidationException("Too many images. Max allowed: " + maxImages);
        }

        if (messageBody.getDocuments() != null
                && messageBody.getDocuments().size() > maxDocuments) {
            throw new ValidationException(
                    "Too many documents. Max allowed: " + maxDocuments);
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

        chat.setTotalSize(chat.getTotalSize() + newSize);
        chatRepository.save(chat);

        return messageRepository.save(messageEntity);
    }

    public List<Message> getChatHistory(UUID chatUuid, User user) {
        Chat chat = getAuthorizedChat(chatUuid, user);
        return messageRepository.findByChatId(chat.getId());
    }

    private long calculateMessageSize(MessageBody body) {
        long size = 0;
        if (body.getText() != null)
            size += body.getText().length();
        if (body.getDocuments() != null) {
            size += body.getDocuments().stream()
                    .mapToLong(d -> d != null ? d.length() : 0).sum();
        }
        if (body.getImages() != null) {
            size += body.getImages().stream().mapToLong(i -> i != null ? i.length() : 0)
                    .sum();
        }
        return size;
    }

    public Page<Chat> getChatsForUser(User user, int page, int size) {
        if (size > maxChatPageSize) {
            size = maxChatPageSize;
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return chatRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

}
