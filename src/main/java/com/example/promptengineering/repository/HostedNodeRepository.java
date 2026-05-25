package com.example.promptengineering.repository;

import com.example.promptengineering.entity.HostedNode;
import com.example.promptengineering.entity.HostedNode.Status;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HostedNodeRepository extends JpaRepository<HostedNode, UUID> {
    Optional<HostedNode> findByAuthToken(String token);
    List<HostedNode> findByStatusAndAllowPublicUseTrue(HostedNode.Status status);
    Optional<HostedNode> findByIdAndStatus(UUID nodeId, Status status);
    List<HostedNode> findByOwnerId(Long ownerId);
    List<HostedNode> findByAllowPublicUseTrueAndStatus(HostedNode.Status status);
}
