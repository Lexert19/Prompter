package com.example.promptengineering.restController;

import com.example.promptengineering.dto.SharedKeyDto;
import com.example.promptengineering.dto.SharedKeyInfoDto;
import com.example.promptengineering.entity.AuditLog;
import com.example.promptengineering.entity.SharedKey;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.ActionType;
import com.example.promptengineering.model.ResultType;
import com.example.promptengineering.service.AuditLogService;
import com.example.promptengineering.service.SharedKeyService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/shared-keys")
public class AdminSharedKeyController {
    private final SharedKeyService sharedKeyService;
    private final AuditLogService auditLogService;

    public AdminSharedKeyController(SharedKeyService sharedKeyService,
        AuditLogService auditLogService) {
        this.sharedKeyService = sharedKeyService;
      this.auditLogService = auditLogService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addSharedKey(@RequestBody SharedKeyDto dto,
                                                            @AuthenticationPrincipal User user) {
        SharedKey savedKey = sharedKeyService.addKey(dto.getProvider(), dto.getKeyValue(), user);
        auditLogService.logAsync(
            auditLogService.createAuditLog(
                user.getId(), user.getEmail(),
                ActionType.SHARED_KEY_GENERATE, ResultType.SUCCESS,
                dto.getProvider(), "Added", null
            )
        );
        return ResponseEntity.ok(Map.of("id", savedKey.getId(), "message", "Added"));
    }

    @GetMapping
    public ResponseEntity<List<SharedKeyInfoDto>> getAllSharedKeys() {
        return ResponseEntity.ok(sharedKeyService.getAllKeys());
    }

    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteSharedKey(@PathVariable Long id,
                                                @RequestBody(required = false) Map<String, Object> dummy,
                                                @AuthenticationPrincipal User user) {
        if (sharedKeyService.deleteKey(id)) {
            AuditLog log = auditLogService.createAuditLog(
                user.getId(),
                user.getEmail(),
                ActionType.SHARED_KEY_DELETE,
                ResultType.SUCCESS,
                id.toString(),
                "Deleted",
                null
            );
            auditLogService.logAsync(log);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
