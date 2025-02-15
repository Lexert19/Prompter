package com.example.promptengineering.restController

import com.example.promptengineering.entity.Project
import com.example.promptengineering.entity.User
import com.example.promptengineering.model.FileElement
import com.example.promptengineering.repository.ProjectRepository
import com.example.promptengineering.repository.UserRepository
import com.example.promptengineering.service.EmbeddingService
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import reactor.core.publisher.Flux



@RestController
@RequestMapping("/api/projects")
class ProjectController(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val embeddingService: EmbeddingService
) {

    @PostMapping
    suspend fun createProject(
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @RequestBody project: Project
    ): ResponseEntity<Project> {
        val userId = oAuth2User.getAttribute<String>("sub")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        
        val savedProject = projectRepository.save(
            project.apply {
                id = UUID.randomUUID().toString()
                this.userId = userId
            }
        ).awaitSingle()
        return ResponseEntity.ok(savedProject)
    }

    @GetMapping("/{projectId}")
    suspend fun getProject(
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @PathVariable projectId: String
    ): Project {
        val userId = oAuth2User.getAttribute<String>("sub")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        
        return projectRepository.findByIdAndUserId(projectId, userId)
            .awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie znaleziony")
    }

  
    @PostMapping("/{projectId}/files")
    suspend fun addFileToProject(
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @PathVariable projectId: String,
        @RequestBody file: FileElement
    ): Project {
        val userId = oAuth2User.getAttribute<String>("sub")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        
        val project = projectRepository.findByIdAndUserId(projectId, userId)
            .awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie znaleziony")
        
        project.files.add(file)
        return projectRepository.save(project).awaitSingle()
    }

    @PostMapping("/{projectId}/index")
    suspend fun indexProject(
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @PathVariable projectId: String
    ): Project {
        val userId = oAuth2User.getAttribute<String>("sub")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        
        val project = projectRepository.findByIdAndUserId(projectId, userId)
            .awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie znaleziony")
        
        val user = userRepository.findById(userId)
            .awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony")
        
        embeddingService.createProjectEmbedding(project, user)
        return projectRepository.save(project).awaitSingle()
    }

    @PostMapping("/chat")
    fun chatWithProject(
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @RequestBody question: String,
        @RequestParam projectId: String
    ): ResponseEntity<Flux<String>> {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build()
    }
}