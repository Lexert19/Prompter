package com.example.promptengineering.restController;

import com.example.promptengineering.entity.Media;
import com.example.promptengineering.exception.ResourceNotFoundException;
import com.example.promptengineering.repository.MediaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/media")
public class MediaApiController {

    private final String uploadDir;
    private final MediaRepository mediaRepository;


    public MediaApiController(@Value("${media.upload-dir}") String uploadDir, MediaRepository mediaRepository) {
        this.uploadDir = uploadDir;
        this.mediaRepository = mediaRepository;
    }

    @GetMapping
    public ResponseEntity<List<Media>> listMedia() {
        List<Media> mediaList = mediaRepository.findAll(Sort.by(Sort.Direction.DESC, "uploadedAt"));
        return ResponseEntity.ok(mediaList);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedia(@PathVariable Long id) throws IOException, ResourceNotFoundException {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));
        Path filePath = Paths.get(media.getFilePath());
        Files.deleteIfExists(filePath);
        mediaRepository.delete(media);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/upload")
    public ResponseEntity<String> uploadMedia(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            return ResponseEntity.badRequest().body("Only image files are allowed");
        }

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID() + extension;
        Path filePath = uploadPath.resolve(storedFilename);
        file.transferTo(filePath.toFile());

        Media media = new Media();
        media.setFileName(originalFilename);
        media.setFilePath(filePath.toString());
        media.setContentType(file.getContentType());
        media.setSize(file.getSize());
        media.setUploadedAt(Instant.now());
        media.setDisplayOrder(0);
        mediaRepository.save(media);

        String imageUrl = "/media/" + storedFilename;
        return ResponseEntity.ok(imageUrl);
    }
}