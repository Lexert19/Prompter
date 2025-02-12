package com.example.promptengineering.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;

import com.example.promptengineering.model.RequestBuilder;

public interface RequestHistoryRepository extends ReactiveCassandraRepository<RequestBuilder, String> {


}
