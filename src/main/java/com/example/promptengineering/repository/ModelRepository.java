package com.example.promptengineering.repository;

import com.example.promptengineering.entity.Model;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends MongoRepository<Model, String> {
    List<Model> findByGlobal(boolean global);
    List<Model> findByUserId(String userId);
}
