package com.example.promptengineering.restController;

import com.example.promptengineering.entity.Media;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.repository.MediaRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class MediaApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MediaApiController mediaApiController;

    @TempDir
    Path tempUploadDir;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() throws Exception {
        Field field = MediaApiController.class.getDeclaredField("uploadDir");
        field.setAccessible(true);
        field.set(mediaApiController, tempUploadDir.toString());

        adminUser = new User();
        adminUser.setEmail("admin@media.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setRoles(List.of(AppRole.ADMIN));
        adminUser = userRepository.save(adminUser);

        normalUser = new User();
        normalUser.setEmail("user@media.com");
        normalUser.setPassword(passwordEncoder.encode("password"));
        normalUser.setRoles(List.of(AppRole.USER));
        normalUser = userRepository.save(normalUser);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (Files.exists(tempUploadDir)) {
            Files.walk(tempUploadDir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception ignored) {}
                    });
        }
    }

    @Test
    void uploadMedia_asAdmin_shouldReturnImageUrlAndSave() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "dummy image content".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/media/upload")
                        .file(file)
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("/media/[a-f0-9-]+\\.jpg")));

        assertThat(Files.list(tempUploadDir)).hasSize(1);
        List<Media> all = mediaRepository.findAll();
        assertThat(all).hasSize(1);
        Media media = all.get(0);
        assertThat(media.getFileName()).isEqualTo("test.jpg");
        assertThat(media.getContentType()).isEqualTo(MediaType.IMAGE_JPEG_VALUE);
        assertThat(media.getSize()).isEqualTo(file.getSize());
        assertThat(media.getDisplayOrder()).isZero();
        assertThat(media.getFilePath()).startsWith(tempUploadDir.toString());
    }

    @Test
    void uploadNonImageFile_asAdmin_shouldReturnBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "text content".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/media/upload")
                        .file(file)
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Only image files are allowed"));

        assertThat(mediaRepository.findAll()).isEmpty();
        assertThat(Files.list(tempUploadDir)).isEmpty();
    }

    @Test
    void uploadEmptyFile_asAdmin_shouldReturnBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/api/admin/media/upload")
                        .file(file)
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("File is empty"));

        assertThat(mediaRepository.findAll()).isEmpty();
    }

    @Test
    void uploadMedia_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/media/upload").file(file))
                .andExpect(status().isUnauthorized());

        assertThat(mediaRepository.findAll()).isEmpty();
    }

    @Test
    void uploadMedia_asUserWithoutAdminRole_shouldReturnForbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "content".getBytes()
        );

        mockMvc.perform(multipart("/api/admin/media/upload")
                        .file(file)
                        .with(user(userService.loadUserByUsername(normalUser.getEmail()))))
                .andExpect(status().isForbidden());

        assertThat(mediaRepository.findAll()).isEmpty();
    }

    @Test
    void listMedia_asAdmin_shouldReturnMediaList() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("file", "img1.jpg", MediaType.IMAGE_JPEG_VALUE, "data1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "img2.png", MediaType.IMAGE_PNG_VALUE, "data2".getBytes());

        mockMvc.perform(multipart("/api/admin/media/upload").file(file1).with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isOk());
        mockMvc.perform(multipart("/api/admin/media/upload").file(file2).with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/media")
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].fileName", is("img2.png")))
                .andExpect(jsonPath("$[1].fileName", is("img1.jpg")));
    }

    @Test
    void listMedia_asUserWithoutAdminRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/media")
                        .with(user(userService.loadUserByUsername(normalUser.getEmail()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteMedia_asAdmin_shouldRemoveFileAndRecord() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "todelete.jpg", MediaType.IMAGE_JPEG_VALUE, "data".getBytes());
        String response = mockMvc.perform(multipart("/api/admin/media/upload")
                        .file(file)
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Media> all = mediaRepository.findAll();
        assertThat(all).hasSize(1);
        Long id = all.get(0).getId();

        mockMvc.perform(delete("/api/admin/media/{id}", id)
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isNoContent());

        assertThat(mediaRepository.findAll()).isEmpty();
        assertThat(Files.list(tempUploadDir)).isEmpty();
    }

    @Test
    void deleteMedia_nonExisting_shouldReturnInternalServerError() throws Exception {
        mockMvc.perform(delete("/api/admin/media/{id}", 999L)
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deleteMedia_asUserWithoutAdminRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/admin/media/{id}", 1L)
                        .with(user(userService.loadUserByUsername(normalUser.getEmail()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteMedia_withoutAuthentication_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(delete("/api/admin/media/{id}", 1L))
                .andExpect(status().isUnauthorized());
    }
}