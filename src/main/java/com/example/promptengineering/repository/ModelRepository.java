package com.example.promptengineering.repository;

import com.example.promptengineering.entity.Model;
import com.example.promptengineering.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {

    List<Model> findByGlobal(boolean global);
    List<Model> findByUser(User user);
}