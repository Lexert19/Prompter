package com.example.promptengineering.controller;

import com.example.promptengineering.entity.Media;
import com.example.promptengineering.repository.MediaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequestMapping("/admin/media")
public class AdminMediaController {

    private final MediaRepository mediaRepository;

    public AdminMediaController(MediaRepository mediaRepository) {
        this.mediaRepository = mediaRepository;
    }

    @GetMapping
    public String listMedia(Model model) {
        List<Media> mediaList = mediaRepository
                .findAll(Sort.by(Sort.Direction.DESC, "uploadedAt"));
        model.addAttribute("mediaList", mediaList);
        return "admin/media/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteMedia(@PathVariable Long id) throws IOException {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));
        Path filePath = Paths.get(media.getFilePath());
        Files.deleteIfExists(filePath);
        mediaRepository.delete(media);
        return "redirect:/admin/media";
    }
}
