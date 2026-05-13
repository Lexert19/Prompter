package com.example.promptengineering.service;

import com.example.promptengineering.dto.UserFileDTO;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.entity.UserFile;
import com.example.promptengineering.exception.FileStorageException;
import com.example.promptengineering.repository.UserFileRepository;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileStorageService {
    private final String uploadDir;
    private final long maxFileSize;
    private final int maxFilesPerUser;

    private final UserFileRepository userFileRepository;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir,
            @Value("${file.max-size}") long maxFileSize,
            @Value("${file.max-count}") int maxFilesPerUser,
            UserFileRepository userFileRepository) {
        this.uploadDir = uploadDir;
        this.maxFileSize = maxFileSize;
        this.maxFilesPerUser = maxFilesPerUser;
        this.userFileRepository = userFileRepository;
    }

    private UserFileDTO toDto(UserFile userFile) {
        return new UserFileDTO(userFile.getId(), userFile.getFileName(),
                userFile.getContentType(), userFile.getSize(),
                userFile.getOwner().getId());
    }

    public UserFileDTO storeFile(MultipartFile file, User owner) throws IOException {
        if (file.getSize() > maxFileSize)
            throw new IllegalArgumentException("Too big");
        if (userFileRepository.countByOwner(owner) >= maxFilesPerUser)
            throw new IllegalArgumentException("Limit");

        Path userDir = Paths.get(uploadDir).toAbsolutePath().normalize()
                .resolve(owner.getId().toString());
        Files.createDirectories(userDir);

        String original = file.getOriginalFilename();
        String displayName = original == null
                ? "file"
                : Paths.get(original).getFileName().toString().replaceAll("[\\p{Cntrl}]",
                        "_");

        String ext = "";
        int dot = displayName.lastIndexOf('.');
        if (dot > 0) {
            ext = displayName.substring(dot).replaceAll("[^a-zA-Z0-9._-]", "");
            if (ext.length() > 20)
                ext = ext.substring(0, 20);
        }

        String baseName = UUID.randomUUID().toString();
        Path binPath = userDir.resolve(baseName + ext).normalize();
        Path b64Path = userDir.resolve(baseName + ".b64").normalize();

        if (!binPath.startsWith(userDir) || !b64Path.startsWith(userDir)) {
            throw new SecurityException("Path traversal");
        }

        try (var in = file.getInputStream()) {
            Files.copy(in, binPath, StandardCopyOption.REPLACE_EXISTING);
        }

        try (var in = Files.newInputStream(binPath);
                var fileOut = Files.newOutputStream(b64Path);
                var base64Out = Base64.getEncoder().wrap(fileOut)) {
            in.transferTo(base64Out);
        }

        UserFile uf = new UserFile();
        uf.setFileName(displayName);
        uf.setStoredPath(binPath.toString());
        uf.setBase64Path(b64Path.toString());
        uf.setContentType(file.getContentType());
        uf.setSize(Files.size(binPath));
        uf.setUploadedAt(Instant.now());
        uf.setOwner(owner);

        return toDto(userFileRepository.save(uf));
    }

    public List<UserFileDTO> getUserFiles(User user) {
        List<UserFile> userFiles = userFileRepository.findByOwner(user);
        return userFiles.stream().map(this::toDto).collect(Collectors.toList());
    }

    public UserFile getUserFile(Long fileId, User owner) throws FileStorageException {
        return userFileRepository.findByIdAndOwner(fileId, owner).orElseThrow(
                () -> new FileStorageException("File not found or access denied"));
    }

    public Path getFilePath(UserFile userFile) {
        return Paths.get(userFile.getStoredPath());
    }
}
