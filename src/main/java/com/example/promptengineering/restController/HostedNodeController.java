package com.example.promptengineering.restController;

import com.example.promptengineering.dto.HostedNodeDto;
import com.example.promptengineering.dto.PublicHostedNodeDto;
import com.example.promptengineering.dto.RegisterNodeRequest;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.service.HostedNodeService;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    public HostedNodeDto create(@AuthenticationPrincipal User user,
                                @RequestBody RegisterNodeRequest req) {
        return HostedNodeDto.fromEntity(service.registerNode(user, req));
    }

    @GetMapping
    public List<HostedNodeDto> myNodes(@AuthenticationPrincipal User user) {
        return service.findByOwner(user).stream().map(HostedNodeDto::fromEntity)
                .collect(Collectors.toList());
    }

    @GetMapping("/public")
    public List<PublicHostedNodeDto> publicNodes() {
        return service.findPublicOnlineNodes().stream()
                .map(PublicHostedNodeDto::fromEntity).collect(Collectors.toList());
    }

    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> delete(@PathVariable UUID id,
                                       @RequestBody(required = false) Map<String, Object> dummy,
                                       @AuthenticationPrincipal User user) {
        service.deleteNode(id, user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/rotate-token")
    public String rotate(@PathVariable UUID id) {
        return "TODO";
    }
}
