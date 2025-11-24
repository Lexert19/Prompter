package com.example.promptengineering.restController;

import com.example.promptengineering.dto.FileElementDTO;
import com.example.promptengineering.entity.FileElement;
import com.example.promptengineering.entity.Project;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.ProjectResponse;
import com.example.promptengineering.model.ScoredFragment;
import com.example.promptengineering.repository.FileElementsRepository;
import com.example.promptengineering.repository.ProjectRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private final UserRepository userRepository;
    private final EmbeddingService embeddingService;
    private final FileElementsRepository fileElementRepository;

    @Autowired
    public ProjectController(ProjectRepository projectRepository,
                             UserRepository userRepository,
                             EmbeddingService embeddingService,
                             FileElementsRepository fileElementRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.embeddingService = embeddingService;
        this.fileElementRepository = fileElementRepository;
    }

    @PostMapping("/create")
    public ResponseEntity<ProjectResponse> createProject(
            @AuthenticationPrincipal User user,
            @RequestBody String name) {

        Project project = new Project();
        project.setName(name);
        project.setUser(user);

        Project savedProject = projectRepository.save(project);
        ProjectResponse projectResponse = new ProjectResponse(
                savedProject.getId(),
                savedProject.getName(),
                new ArrayList<>()
        );

        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId) {

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        List<FileElementDTO> files = fileElementRepository.findByProject(project)
                .stream()
                .map(file -> new FileElementDTO(file.getId(), file.getName()))
                .collect(Collectors.toList());

        ProjectResponse projectResponse = new ProjectResponse(project.getId(), project.getName(), files);
        return ResponseEntity.ok(projectResponse);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getUserProjects(@AuthenticationPrincipal User user) {
        List<Project> projects = projectRepository.findAllByUser(user);

        List<ProjectResponse> projectResponses = projects.stream()
                .map(project -> new ProjectResponse(
                        project.getId(),
                        project.getName(),
                        new ArrayList<>()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(projectResponses);
    }

    @PostMapping("/{projectId}/files")
    public ResponseEntity<ProjectResponse> addFileToProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId,
            @RequestBody FileElement file) {

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found"));

        embeddingService.addFileToProject(project, file, user);

        Project updatedProject = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        List<FileElementDTO> files = fileElementRepository.findByProject(updatedProject)
                .stream()
                .map(f -> new FileElementDTO(f.getId(), f.getName()))
                .collect(Collectors.toList());

        ProjectResponse projectResponse = new ProjectResponse(updatedProject.getId(), updatedProject.getName(), files);
        return ResponseEntity.ok(projectResponse);
    }

    @DeleteMapping("/{projectId}/files/{fileId}")
    public ResponseEntity<Void> deleteFileFromProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId,
            @PathVariable Long fileId) {

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie istnieje lub nie należy do Ciebie"));

        FileElement file = fileElementRepository.findByIdAndProject(fileId, project)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plik nie znaleziony"));

        fileElementRepository.delete(file);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/files")
    public ResponseEntity<List<FileElement>> getProjectFiles(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId) {

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie istnieje lub nie należy do Ciebie"));

        List<FileElement> files = fileElementRepository.findByProject(project);

        return ResponseEntity.ok(files);
    }

    @GetMapping("/{projectId}/files/{fileId}")
    public ResponseEntity<String> getFile(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId,
            @PathVariable Long fileId) {

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie istnieje lub nie należy do Ciebie"));

        FileElement file = fileElementRepository.findByIdAndProject(fileId, project)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plik nie znaleziony"));

        return ResponseEntity.ok(file.getContent());
    }

    @PostMapping("/{projectId}/similar-fragments")
    public ResponseEntity<List<ScoredFragment>> getSimilarFragments(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId,
            @RequestParam String query) {

        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie znaleziony"));

        List<ScoredFragment> similarFragments = embeddingService.retrieveSimilarFragments(query, project, user);
        return ResponseEntity.ok(similarFragments);
    }
}