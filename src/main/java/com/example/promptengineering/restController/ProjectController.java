package com.example.promptengineering.restController;

import com.example.promptengineering.dto.UserFileDTO;
import com.example.promptengineering.entity.Project;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.entity.UserFile;
import com.example.promptengineering.model.ProjectResponse;
import com.example.promptengineering.model.ScoredFragment;
import com.example.promptengineering.repository.ProjectRepository;
import com.example.promptengineering.repository.UserFileRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.EmbeddingService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectRepository projectRepository;
    private final EmbeddingService embeddingService;
    private final UserFileRepository userFileRepository;

    @Autowired
    public ProjectController(ProjectRepository projectRepository,
            UserRepository userRepository, EmbeddingService embeddingService,
            UserFileRepository userFileRepository) {
        this.projectRepository = projectRepository;
        this.embeddingService = embeddingService;
        this.userFileRepository = userFileRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<ProjectResponse> createProject(@AuthenticationPrincipal User user,
                                                         @RequestBody String name) {

        Project project = new Project();
        project.setName(name);
        project.setUser(user);

        Project savedProject = projectRepository.save(project);
        ProjectResponse projectResponse = new ProjectResponse(savedProject.getId(),
                savedProject.getName(), new ArrayList<>());

        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(@AuthenticationPrincipal User user,
                                                      @PathVariable Long projectId) {

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Project not found"));

        List<UserFileDTO> files = userFileRepository.findByProject(project).stream()
                .map(UserFileDTO::fromEntity).collect(Collectors.toList());

        ProjectResponse projectResponse = new ProjectResponse(project.getId(),
                project.getName(), files);
        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getUserProjects(@AuthenticationPrincipal User user) {
        List<Project> projects = projectRepository.findAllByUser(user);

        List<ProjectResponse> projectResponses = projects
                .stream().map(project -> new ProjectResponse(project.getId(),
                        project.getName(), new ArrayList<>()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(projectResponses);
    }

    @PostMapping("/{projectId}/files")
    public ResponseEntity<ProjectResponse> addFileToProject(@AuthenticationPrincipal User user,
                                                            @PathVariable Long projectId,
                                                            @RequestBody java.util.Map<String, Long> payload) {

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Project not found"));

        Long fileId = payload.get("fileId");
        if (fileId == null) {
            return ResponseEntity.badRequest().build();
        }

        embeddingService.addFileToProject(project, fileId, user);

        Project updatedProject = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        List<UserFileDTO> files = userFileRepository.findByProject(updatedProject)
                .stream().map(UserFileDTO::fromEntity).collect(Collectors.toList());

        return ResponseEntity.ok(new ProjectResponse(updatedProject.getId(),
                updatedProject.getName(), files));
    }

    @DeleteMapping("/{projectId}/files/{fileId}")
    public ResponseEntity<Void> deleteFileFromProject(@AuthenticationPrincipal User user,
                                                      @PathVariable Long projectId,
                                                      @PathVariable Long fileId)
            throws IOException {

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Project not found or you are not the owner"));

        UserFile userFile = userFileRepository.findByIdAndProject(fileId, project)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "File not found in this project"));

        Path path = Paths.get(userFile.getStoredPath());
        Files.deleteIfExists(path);
        Path base64Path = Paths.get(userFile.getBase64Path());
        Files.deleteIfExists(base64Path);
        userFileRepository.delete(userFile);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/files")
    public ResponseEntity<List<UserFileDTO>> getProjectFiles(@AuthenticationPrincipal User user,
                                                             @PathVariable Long projectId) {
        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Project not found or you are not the owner"));

        List<UserFile> files = userFileRepository.findByProject(project);
        List<UserFileDTO> fileDtos = files.stream().map(UserFileDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(fileDtos);
    }

    @GetMapping("/{projectId}/files/{fileId}")
    public ResponseEntity<Resource> getFile(@AuthenticationPrincipal User user,
                                            @PathVariable Long projectId,
                                            @PathVariable Long fileId)
            throws MalformedURLException {

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Project not found or you are not the owner"));

        UserFile userFile = userFileRepository.findByIdAndProject(fileId, project)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "File not found"));

        Path path = Paths.get(userFile.getStoredPath());
        Resource resource = new org.springframework.core.io.UrlResource(path.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(userFile.getContentType()))
                .body(resource);
    }

    @PostMapping("/{projectId}/similar-fragments")
    public ResponseEntity<List<ScoredFragment>> getSimilarFragments(@AuthenticationPrincipal User user,
                                                                    @PathVariable Long projectId,
                                                                    @RequestParam String query) {

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Project not found"));

        List<ScoredFragment> similarFragments = embeddingService
                .retrieveSimilarFragments(query, project, user);
        return ResponseEntity.ok(similarFragments);
    }
}
