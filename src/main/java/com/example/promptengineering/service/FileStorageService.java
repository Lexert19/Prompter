package com.example.promptengineering.service;

import com.example.promptengineering.dto.UserFileDTO;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.entity.UserFile;
import com.example.promptengineering.exception.FileStorageException;
import com.example.promptengineering.repository.UserFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private final UserFileRepository userFileRepository;

    public FileStorageService(UserFileRepository userFileRepository) {
        this.userFileRepository = userFileRepository;
    }

    private UserFileDTO toDto(UserFile userFile) {
        return new UserFileDTO(
                userFile.getId(),
                userFile.getFileName(),
                userFile.getContentType(),
                userFile.getSize(),
                userFile.getOwner().getId()
        );
    }


    public UserFileDTO storeFile(MultipartFile file, User owner) throws IOException {
        Path userDir = Paths.get(uploadDir, owner.getId().toString());
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
        }

        byte[] fileBytes = file.getBytes();

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String binaryFilename = UUID.randomUUID() + extension;
        Path binaryPath = userDir.resolve(binaryFilename);
        file.transferTo(binaryPath.toFile());

        String base64Content = Base64.getEncoder().encodeToString(fileBytes);

        String base64Filename = UUID.randomUUID() + ".b64";
        Path base64Path = userDir.resolve(base64Filename);
        Files.writeString(base64Path, base64Content, StandardCharsets.UTF_8);

        UserFile userFile = new UserFile();
        userFile.setFileName(originalFilename);
        userFile.setStoredPath(binaryPath.toString());
        userFile.setBase64Path(base64Path.toString());
        userFile.setContentType(file.getContentType());
        userFile.setSize(file.getSize());
        userFile.setUploadedAt(Instant.now());
        userFile.setOwner(owner);

        UserFile savedFile = userFileRepository.save(userFile);
        return toDto(savedFile);
    }

    public UserFile getUserFile(Long fileId, User owner) throws FileStorageException {
        return userFileRepository.findByIdAndOwner(fileId, owner)
                .orElseThrow(() -> new FileStorageException("File not found or access denied"));
    }

    public Path getFilePath(UserFile userFile) {
        return Paths.get(userFile.getStoredPath());
    }
}