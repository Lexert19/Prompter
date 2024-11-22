package com.example.promptengineering.repository;

import org.springframework.data.cassandra.repository.ReactiveCassandraRepository;
import org.springframework.stereotype.Repository;

import com.example.promptengineering.entity.User;

@Repository
public interface UserRepository extends ReactiveCassandraRepository<User, String>{

}
