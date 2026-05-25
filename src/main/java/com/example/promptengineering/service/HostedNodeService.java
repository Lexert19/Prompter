package com.example.promptengineering.service;

import com.example.promptengineering.dto.RegisterNodeRequest;
import com.example.promptengineering.entity.HostedNode;
import com.example.promptengineering.entity.HostedNode.Status;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.HostedNodeRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class HostedNodeService {

    private final HostedNodeRepository nodeRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public HostedNode registerNode(User owner, RegisterNodeRequest req) {
        if (req.nodeName() == null || req.nodeName().isBlank()) {
            throw new IllegalArgumentException("nodeName required");
        }

        HostedNode node = new HostedNode();
        node.setOwner(owner);
        node.setNodeName(req.nodeName().trim());
        node.setModelName(req.modelName());
        node.setModelFamily(req.modelFamily());
        node.setAllowPublicUse(
                req.allowPublicUse() != null ? req.allowPublicUse() : true);
        node.setStatus(Status.OFFLINE);
        node.setAuthToken(generateToken());

        return nodeRepository.save(node);
    }

    @Transactional(readOnly = true)
    public List<HostedNode> findByOwner(User owner) {
        return nodeRepository.findByOwnerId(owner.getId());
    }

    @Transactional(readOnly = true)
    public List<HostedNode> findPublicOnlineNodes() {
        return nodeRepository.findByAllowPublicUseTrueAndStatus(Status.ONLINE);
    }

    @Transactional
    public void deleteNode(UUID id, User requester) {
        HostedNode node = nodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Node not found"));

        if (!node.getOwner().getId().equals(requester.getId())) {
            throw new AccessDeniedException("Access denied");
        }
        nodeRepository.delete(node);
    }

    @Transactional
    public String rotateToken(HostedNode node) {
        String newToken = generateToken();
        node.setAuthToken(newToken);
        nodeRepository.save(node);
        return newToken;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
