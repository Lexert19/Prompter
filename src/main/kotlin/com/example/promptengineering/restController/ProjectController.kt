package com.example.promptengineering.restController

import com.example.promptengineering.entity.Project
import com.example.promptengineering.entity.FileElement
import com.example.promptengineering.entity.User
import com.example.promptengineering.repository.ProjectRepository
import com.example.promptengineering.repository.UserRepository
import com.example.promptengineering.service.EmbeddingService
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import kotlinx.coroutines.reactive.awaitSingle
import com.example.promptengineering.model.ProjectResponse
import kotlin.collections.ArrayList
import com.example.promptengineering.model.ScoredFragment
import com.example.promptengineering.repository.FileElementsRepository
import java.security.Principal


@RestController
@RequestMapping("/api/projects")
class ProjectController(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val embeddingService: EmbeddingService,
    private val fileElementRepository: FileElementsRepository
) {

    @PostMapping("/create")
    suspend fun createProject(
        @AuthenticationPrincipal user: User,
        @RequestBody name: String
    ): ResponseEntity<ProjectResponse> {
        val project = Project().apply {
            this.name = name
            this.userId = user.id
        }
    
        val savedProject = projectRepository.save(project)
        val projectResponse = ProjectResponse(savedProject.id, savedProject.name, ArrayList())
        return ResponseEntity.ok(projectResponse)
    }

    @GetMapping("/{projectId}")
    suspend fun getProject(
        @AuthenticationPrincipal user: User,
        @PathVariable projectId: String
    ): ResponseEntity<ProjectResponse> {
        val project = projectRepository.findByIdAndUserId(projectId, user.id)
        if(project.isEmpty)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found")

        val files = fileElementRepository.findByProject(project.get().id)

        val projectResponse = ProjectResponse(project.get().id, project.get().name, files)
        return ResponseEntity.ok(projectResponse)
    }

    @GetMapping
    suspend fun getUserProjects(
        @AuthenticationPrincipal user: User
    ): ResponseEntity<List<ProjectResponse>> {
        val projects = projectRepository.findAllByUserId(user.id)

        val projectResponses = projects.map { project ->
            ProjectResponse(
                id = project.id,
                name = project.name,
                files = ArrayList()
            )
        }
        
        return ResponseEntity.ok(projectResponses)
    }

  
    @PostMapping("/{projectId}/files")
    suspend fun addFileToProject(
        @AuthenticationPrincipal user: User,
        @PathVariable projectId: String,
        @RequestBody file: FileElement
    ): ResponseEntity<ProjectResponse> {
        val project = projectRepository.findByIdAndUserId(projectId, user.id)
        if(project.isEmpty)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found")

        embeddingService.addFileToProject(project.get(), file, user)

        val updatedProject = projectRepository.findByIdAndUserId(projectId, user.id)
        if(updatedProject.isEmpty)
            throw  Exception("Project not found");
        val files = fileElementRepository.findByProject(project.get().id)

        val projectResponse = ProjectResponse(updatedProject.get().id, updatedProject.get().name, files)
        return ResponseEntity.ok(projectResponse)
    }

    @GetMapping("/{projectId}/files")
    fun getProjectFiles(
        @AuthenticationPrincipal user: User,
        @PathVariable projectId: String
    ): ResponseEntity<List<FileElement>> {
        val project = projectRepository.findByIdAndUserId(projectId, user.id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie istnieje lub nie należy do Ciebie") }

        val files = fileElementRepository.findByProject(project.id)

        return ResponseEntity.ok(files)
    }


    @GetMapping("/{projectId}/files/{fileId}")
    fun getFile(
        @AuthenticationPrincipal user: User,
        @PathVariable projectId: String,
        @PathVariable fileId: String
    ): ResponseEntity<String> {
        val project = projectRepository.findByIdAndUserId(projectId, user.id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie istnieje lub nie należy do Ciebie") }

        val file = fileElementRepository.findByIdAndProject(fileId, project.id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Plik nie znaleziony") }

        return ResponseEntity.ok(file.content)
    }

    @PostMapping("/{projectId}/similar-fragments")
    fun getSimilarFragments(
        @AuthenticationPrincipal user: User,
        @PathVariable projectId: String,
        @RequestParam query: String
    ): ResponseEntity<List<ScoredFragment>> {
        val project = projectRepository.findByIdAndUserId(projectId, user.id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie znaleziony") }

        val similarFragments = embeddingService.retrieveSimilarFragments(query, project, user)
        return ResponseEntity.ok(similarFragments)
    }

}