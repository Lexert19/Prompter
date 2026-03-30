package com.example.promptengineering.restController;

import com.example.promptengineering.dto.SharedKeyDto;
import com.example.promptengineering.dto.SharedKeyInfoDto;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.service.SharedKeyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/shared-keys")
public class AdminSharedKeyController {
    private final SharedKeyService sharedKeyService;

    public AdminSharedKeyController(SharedKeyService sharedKeyService) {
        this.sharedKeyService = sharedKeyService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> addSharedKey(@RequestBody SharedKeyDto dto,
            @AuthenticationPrincipal User user) {
        sharedKeyService.addKey(dto.getProvider(), dto.getKeyValue(), user);
        return ResponseEntity.ok(Map.of("message", "Added"));
    }

    @GetMapping
    public ResponseEntity<List<SharedKeyInfoDto>> getAllSharedKeys() {
        return ResponseEntity.ok(sharedKeyService.getAllKeys());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSharedKey(@PathVariable Long id) {
        if (sharedKeyService.deleteKey(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
