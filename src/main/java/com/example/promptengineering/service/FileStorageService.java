package com.example.promptengineering.service;

import com.example.promptengineering.dto.UserFileDTO;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.entity.UserFile;
import com.example.promptengineering.exception.FileStorageException;
import com.example.promptengineering.repository.UserFileRepository;
import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FileStorageService {
    private final MinioClient minioClient;
    private final String bucket;
    private final long maxFileSize;
    private final int maxFilesPerUser;
    private final UserFileRepository userFileRepository;

    public FileStorageService(MinioClient minioClient,
        @Value("${minio.bucket}") String bucket,
        @Value("${file.max-size}") long maxFileSize,
        @Value("${file.max-count}") int maxFilesPerUser,
        UserFileRepository userFileRepository) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        this.maxFileSize = maxFileSize;
        this.maxFilesPerUser = maxFilesPerUser;
        this.userFileRepository = userFileRepository;
    }

    @PostConstruct
    public void init() throws Exception {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
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

        String original = file.getOriginalFilename();
        String displayName = original == null
            ? "file"
            : java.nio.file.Paths.get(original).getFileName().toString().replaceAll("[\\p{Cntrl}]", "_");

        String ext = "";
        int dot = displayName.lastIndexOf('.');
        if (dot > 0) {
            ext = displayName.substring(dot).replaceAll("[^a-zA-Z0-9._-]", "");
            if (ext.length() > 20) ext = ext.substring(0, 20);
        }

        String baseName = UUID.randomUUID().toString();
        String binObject = owner.getId().toString() + "/" + baseName + ext;
        String b64Object = owner.getId().toString() + "/" + baseName + ".b64";

        if (binObject.contains("..") || b64Object.contains("..")) {
            throw new SecurityException("Path traversal");
        }

        try {
            byte[] fileBytes = file.getBytes();

            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket).object(binObject)
                    .stream(new ByteArrayInputStream(fileBytes), (long) fileBytes.length, (long) -1)
                    .contentType(file.getContentType())
                    .build()
            );

            String b64String = Base64.getEncoder().encodeToString(fileBytes);
            byte[] b64Bytes = b64String.getBytes(StandardCharsets.UTF_8);
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucket).object(b64Object)
                    .stream(new ByteArrayInputStream(b64Bytes), (long) b64Bytes.length, (long) -1)
                    .contentType("text/plain")
                    .build()
            );

            UserFile uf = new UserFile();
            uf.setFileName(displayName);
            uf.setStoredPath(binObject);
            uf.setBase64Path(b64Object);
            uf.setContentType(file.getContentType());
            uf.setSize((long) fileBytes.length);
            uf.setUploadedAt(Instant.now());
            uf.setOwner(owner);

            return toDto(userFileRepository.save(uf));

        } catch (Exception e) {
            throw new IOException("MinIO store failed", e);
        }
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
        try {
            InputStream is = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucket).object(userFile.getStoredPath()).build()
            );
            Path temp = Files.createTempFile("minio-", "-" + java.nio.file.Paths.get(userFile.getStoredPath()).getFileName());
            Files.copy(is, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            is.close();
            return temp;
        } catch (Exception e) {
            throw new RuntimeException("Failed to download " + userFile.getStoredPath(), e);
        }
    }

    public String getBase64Content(UserFile userFile) {
        try (InputStream is = minioClient.getObject(
            GetObjectArgs.builder().bucket(bucket).object(userFile.getBase64Path()).build())) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read b64 " + userFile.getBase64Path(), e);
        }
    }
}