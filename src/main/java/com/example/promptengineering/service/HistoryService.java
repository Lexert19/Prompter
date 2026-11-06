package com.example.promptengineering.service;

import com.example.promptengineering.entity.Chat;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.exception.ResourceNotFoundException;
import com.example.promptengineering.exception.UserSecurityException;
import com.example.promptengineering.repository.ChatRepository;
import com.example.promptengineering.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class HistoryService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    @Value("${app.chat.page.max-size}")
    private int maxChatPageSize;

    public HistoryService(ChatRepository chatRepository,
            MessageRepository messageRepository) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
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
        if (!isUserAuthorizedForChat(chat, user)) {
            throw new UserSecurityException(
                    "User is not authorized to delete this chat.");
        }
        chatRepository.delete(chat);
    }

    @Transactional
    public void deleteAllChats(User user) {
        List<Chat> chats = chatRepository.findByUser(user);
        for (Chat chat : chats) {
            messageRepository.deleteByChatId(chat.getId());
        }
        chatRepository.deleteAll(chats);
    }

    public Page<Chat> getChatsForUser(User user, int page, int size) {
        if (size > maxChatPageSize)
            size = maxChatPageSize;
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return chatRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    private boolean isUserAuthorizedForChat(Chat chat, User user) {
        return chat.getUser() != null && chat.getUser().getId().equals(user.getId());
    }
}
