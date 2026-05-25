package com.example.promptengineering.restController;

import com.example.promptengineering.dto.RegisterNodeRequest;
import com.example.promptengineering.entity.HostedNode;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.service.HostedNodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/nodes")
@RequiredArgsConstructor
public class HostedNodeController {
    private final HostedNodeService service;

    @PostMapping
    public HostedNode create(@AuthenticationPrincipal User user,
                             @RequestBody RegisterNodeRequest req) {
        return service.registerNode(user, req);
    }

    @GetMapping
    public List<HostedNode> myNodes(@AuthenticationPrincipal User user) {
        return service.findByOwner(user);
    }

    @GetMapping("/public")
    public List<HostedNode> publicNodes() {
        return service.findPublicOnlineNodes();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @AuthenticationPrincipal User user) {
        service.deleteNode(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rotate-token")
    public String rotate(@PathVariable UUID id) {
        return "TODO";
    }
}
