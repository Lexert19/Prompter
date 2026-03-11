package com.example.promptengineering.restController;

import com.example.promptengineering.dto.PostDto;
import com.example.promptengineering.entity.Post;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.repository.PostRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class BlogApiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User adminUser;
    private User normalUser;
    private Post testPost1;
    private Post testPost2;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode("password"));
        adminUser.setRoles(List.of(AppRole.ADMIN));
        adminUser = userRepository.save(adminUser);

        normalUser = new User();
        normalUser.setEmail("user@example.com");
        normalUser.setPassword(passwordEncoder.encode("password"));
        normalUser.setRoles(List.of(AppRole.USER));
        normalUser = userRepository.save(normalUser);

        testPost1 = new Post();
        testPost1.setTitle("First Post");
        testPost1.setSlug("first-post");
        testPost1.setContent("Content of first post");
        testPost1.setLang("pl");
        testPost1.setCreatedAt(LocalDateTime.now());
        testPost1.setUpdatedAt(LocalDateTime.now());
        testPost1 = postRepository.save(testPost1);

        testPost2 = new Post();
        testPost2.setTitle("Second Post");
        testPost2.setSlug("second-post");
        testPost2.setContent("Content of second post");
        testPost2.setLang("en");
        testPost2.setCreatedAt(LocalDateTime.now());
        testPost2.setUpdatedAt(LocalDateTime.now());
        testPost2 = postRepository.save(testPost2);
    }

    @Test
    void shouldReturnAllPostsAsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/blog/posts")
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder("First Post", "Second Post")));
    }

    @Test
    void shouldReturnPostByIdAsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/blog/posts/{id}", testPost1.getId())
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testPost1.getId().intValue())))
                .andExpect(jsonPath("$.title", is("First Post")))
                .andExpect(jsonPath("$.slug", is("first-post")))
                .andExpect(jsonPath("$.lang", is("pl")));
    }

    @Test
    void shouldReturn404WhenPostNotFoundAsAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/blog/posts/{id}", 999L)
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateNewPostAsAdmin() throws Exception {
        PostDto newPost = new PostDto();
        newPost.setTitle("New Post");
        newPost.setSlug("new-post");
        newPost.setContent("New content");
        newPost.setLang("en");

        mockMvc.perform(post("/api/admin/blog/posts")
                        .with(user(userService.loadUserByUsername(adminUser.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPost)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Post")))
                .andExpect(jsonPath("$.slug", is("new-post")))
                .andExpect(jsonPath("$.content", is("New content")))
                .andExpect(jsonPath("$.lang", is("en")));
    }

    @Test
    void shouldCreateNewPostWithParentNullAsAdmin() throws Exception {
        PostDto newPost = new PostDto();
        newPost.setTitle("New Post");
        newPost.setSlug("new-post");
        newPost.setParentId(null);
        newPost.setContent("New content");
        newPost.setLang("en");

        mockMvc.perform(post("/api/admin/blog/posts")
                        .with(user(userService.loadUserByUsername(adminUser.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPost)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("New Post")))
                .andExpect(jsonPath("$.slug", is("new-post")))
                .andExpect(jsonPath("$.parentId").doesNotExist())
                .andExpect(jsonPath("$.content", is("New content")))
                .andExpect(jsonPath("$.lang", is("en")));
    }

    @Test
    void shouldUpdateExistingPostAsAdmin() throws Exception {
        PostDto updatedPost = new PostDto();
        updatedPost.setTitle("Updated Title");
        updatedPost.setSlug("updated-slug");
        updatedPost.setContent("Updated content");
        updatedPost.setLang("pl");

        mockMvc.perform(put("/api/admin/blog/posts/{id}", testPost1.getId())
                        .with(user(userService.loadUserByUsername(adminUser.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPost)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(testPost1.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.slug", is("updated-slug")))
                .andExpect(jsonPath("$.content", is("Updated content")))
                .andExpect(jsonPath("$.lang", is("pl")));
    }


    @Test
    void shouldReturn404WhenUpdatingNonExistentPostAsAdmin() throws Exception {
        PostDto updatedPost = new PostDto();
        updatedPost.setTitle("Updated Title");

        mockMvc.perform(put("/api/admin/blog/posts/{id}", 999L)
                        .with(user(userService.loadUserByUsername(adminUser.getEmail())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedPost)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeletePostAsAdmin() throws Exception {
        mockMvc.perform(delete("/api/admin/blog/posts/{id}", testPost1.getId())
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/admin/blog/posts/{id}", testPost1.getId())
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentPostAsAdmin() throws Exception {
        mockMvc.perform(delete("/api/admin/blog/posts/{id}", 999L)
                        .with(user(userService.loadUserByUsername(adminUser.getEmail()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/blog/posts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenUserIsNotAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/blog/posts")
                        .with(user(userService.loadUserByUsername(normalUser.getEmail()))))
                .andExpect(status().isForbidden());
    }
}