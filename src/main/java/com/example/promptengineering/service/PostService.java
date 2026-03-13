package com.example.promptengineering.service;

import com.example.promptengineering.dto.PostDto;
import com.example.promptengineering.entity.Media;
import com.example.promptengineering.entity.Post;
import com.example.promptengineering.exception.ResourceNotFoundException;
import com.example.promptengineering.repository.MediaRepository;
import com.example.promptengineering.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final MediaRepository mediaRepository;

    public PostService(PostRepository postRepository, MediaRepository mediaRepository) {
        this.postRepository = postRepository;
        this.mediaRepository = mediaRepository;
    }

    public List<PostDto> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public PostDto getPostDtoById(Long id) throws ResourceNotFoundException {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        return mapToDto(post);
    }

    public Post getPostById(Long id) throws ResourceNotFoundException {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    public PostDto createPost(PostDto postDto) throws ResourceNotFoundException {
        Post post = mapToEntity(postDto);
        post.setCreatedAt(LocalDateTime.now());
        post.setSlug(postDto.getSlug());
        post.setContent(postDto.getContent());
        post.setThumbnailId(postDto.getThumbnailId());
        post.setShortDescription(postDto.getShortDescription());
        if (postDto.getParentId() != null) {
            Post parent = postRepository.findById(postDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent post not found"));
            post.setParent(parent);
        }
        post.setUpdatedAt(LocalDateTime.now());
        Post saved = postRepository.save(post);
        return mapToDto(saved);
    }

    public PostDto updatePost(Long id, PostDto postDto) throws ResourceNotFoundException {
        Post post = this.getPostById(id);
        post.setTitle(postDto.getTitle());
        post.setSlug(postDto.getSlug());
        post.setContent(postDto.getContent());
        post.setThumbnailId(postDto.getThumbnailId());
        post.setShortDescription(postDto.getShortDescription());
        if(postDto.getParentId() != null){
            Post parent = postRepository.findById(postDto.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent post not found"));
            post.setParent(parent);
        } else {
            post.setParent(null);
        }
        post.setUpdatedAt(LocalDateTime.now());
        Post saved = postRepository.save(post);
        return mapToDto(saved);
    }

    public void deletePost(Long id) throws ResourceNotFoundException {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
        postRepository.delete(post);
    }

    public List<PostDto> getTranslations(Long parentId) {
        return postRepository.findByParentId(parentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    private PostDto mapToDto(Post post) {
        PostDto dto = new PostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setSlug(post.getSlug());
        dto.setContent(post.getContent());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setLang(post.getLang());
        dto.setThumbnailId(post.getThumbnailId());
        dto.setShortDescription(post.getShortDescription());
        if (post.getParent() != null) {
            dto.setParentId(post.getParent().getId());
        }
        if (post.getThumbnailId() != null) {
            Media media = mediaRepository.findById(post.getThumbnailId()).orElse(null);
            if (media != null) {
                String storedFilename = Paths.get(media.getFilePath()).getFileName().toString();
                dto.setThumbnailUrl("/media/" + storedFilename);
            }
        }
        return dto;
    }

    private Post mapToEntity(PostDto dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setSlug(dto.getSlug());
        post.setContent(dto.getContent());
        post.setLang(dto.getLang());
        post.setThumbnailId(dto.getThumbnailId());
        return post;
    }

    public PostDto getPostDtoBySlug(String slug) throws ResourceNotFoundException {
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return mapToDto(post);
    }
}