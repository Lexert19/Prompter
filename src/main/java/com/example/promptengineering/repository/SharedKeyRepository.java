package com.example.promptengineering.repository;

import com.example.promptengineering.entity.SharedKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SharedKeyRepository extends JpaRepository<SharedKey, Long> {
    List<SharedKey> findByProviderAndWorkingTrue(String provider);
    List<SharedKey> findByProvider(String provider);
    @Query("SELECT sk FROM SharedKey sk JOIN FETCH sk.owner WHERE sk.id = :id")
    Optional<SharedKey> findByIdWithOwner(@Param("id") Long id);
}
