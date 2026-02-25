package com.example.promptengineering.service;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.entity.UserFile;
import com.example.promptengineering.exception.FileStorageException;
import com.example.promptengineering.repository.UserFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private final UserFileRepository userFileRepository;

    public FileStorageService(UserFileRepository userFileRepository) {
        this.userFileRepository = userFileRepository;
    }

    public UserFile storeFile(MultipartFile file, User owner) throws IOException {
        Path userDir = Paths.get(uploadDir, owner.getId().toString());
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID() + extension;
        Path targetPath = userDir.resolve(storedFilename);

        file.transferTo(targetPath.toFile());

        UserFile userFile = new UserFile();
        userFile.setFileName(originalFilename);
        userFile.setStoredPath(targetPath.toString());
        userFile.setContentType(file.getContentType());
        userFile.setSize(file.getSize());
        userFile.setUploadedAt(Instant.now());
        userFile.setOwner(owner);

        return userFileRepository.save(userFile);
    }

    public UserFile getUserFile(Long fileId, User owner) throws FileStorageException {
        return userFileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new FileStorageException("File not found or access denied"));
    }

    public Path getFilePath(UserFile userFile) {
        return Paths.get(userFile.getStoredPath());
    }
}