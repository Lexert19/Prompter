package com.example.promptengineering.repository;

import org.springframework.data.cassandra.repository.Query;
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

import com.example.promptengineering.entity.ChatHistory;

import reactor.core.publisher.Flux;

@Repository
public interface ChatHistoryRepository extends ReactiveCassandraRepository<ChatHistory, String> {
    @Query("SELECT * FROM chathistory WHERE userid = ?0 ALLOW FILTERING")
    Flux<ChatHistory> findAllByUserId(String userId);
}
