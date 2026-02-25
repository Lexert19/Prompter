package com.example.promptengineering.restController;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.entity.UserFile;
import com.example.promptengineering.exception.FileStorageException;
import com.example.promptengineering.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UserFile> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws IOException {

        UserFile savedFile = fileStorageService.storeFile(file, user);
        return ResponseEntity.ok(savedFile);
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal User user) throws IOException, FileStorageException {

        UserFile userFile = fileStorageService.getUserFile(fileId, user);
        Path filePath = fileStorageService.getFilePath(userFile);

        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String contentType = userFile.getContentType();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + userFile.getFileName() + "\"")
                .body(resource);
    }
}