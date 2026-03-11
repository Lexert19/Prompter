package com.example.promptengineering.repository;

import com.example.promptengineering.entity.SharedKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SharedKeyRepository extends JpaRepository<SharedKey, Long> {
    List<SharedKey> findByProviderAndWorkingTrue(String provider);
    List<SharedKey> findByProvider(String provider);

}
