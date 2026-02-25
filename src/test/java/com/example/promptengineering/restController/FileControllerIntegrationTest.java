package com.example.promptengineering.restController;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserFileRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFileRepository userFileRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @TempDir
    Path tempUploadDir;

    private User user1;
    private User user2;
    private final String user1Email = "fileuser1@example.com";
    private final String user2Email = "fileuser2@example.com";
    private final String password = "password";

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setEmail(user1Email);
        user1.setPassword(passwordEncoder.encode(password));
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setEmail(user2Email);
        user2.setPassword(passwordEncoder.encode(password));
        user2 = userRepository.save(user2);

        System.setProperty("file.upload-dir", tempUploadDir.toString());
    }

    @Test
    void uploadFile_shouldReturnSavedFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .with(user(userService.loadUserByUsername(user1Email))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("test.txt"))
                .andExpect(jsonPath("$.contentType").value(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(jsonPath("$.size").value(13))
                .andExpect(jsonPath("$.owner.id").value(user1.getId()))
                .andExpect(jsonPath("$.storedPath").exists());
    }

    @Test
    void downloadFile_ownFile_shouldReturnFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "data.bin",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                new byte[]{1, 2, 3, 4, 5}
        );

        String uploadResponse = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .with(user(userService.loadUserByUsername(user1Email))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long fileId = objectMapper.readTree(uploadResponse).get("id").asLong();

        mockMvc.perform(get("/api/files/{fileId}", fileId)
                        .with(user(userService.loadUserByUsername(user1Email))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"data.bin\""))
                .andExpect(content().bytes(new byte[]{1, 2, 3, 4, 5}));
    }

    @Test
    void downloadFile_otherUserFile_shouldReturn404() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "secret.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "for user1 only".getBytes()
        );

        String uploadResponse = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .with(user(userService.loadUserByUsername(user1Email))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long fileId = objectMapper.readTree(uploadResponse).get("id").asLong();

        mockMvc.perform(get("/api/files/{fileId}", fileId)
                        .with(user(userService.loadUserByUsername(user2Email))))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadFile_notAuthenticated_shouldReturn401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "public.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "content".getBytes()
        );

        String uploadResponse = mockMvc.perform(multipart("/api/files/upload")
                        .file(file)
                        .with(user(userService.loadUserByUsername(user1Email))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long fileId = objectMapper.readTree(uploadResponse).get("id").asLong();

        mockMvc.perform(get("/api/files/{fileId}", fileId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void uploadFile_withoutAuthentication_shouldReturn401() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "content".getBytes()
        );

        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isUnauthorized());
    }
}