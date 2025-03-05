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

    @PostMapping("/create")
    suspend fun createProject(
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @RequestBody name: String
    ): ResponseEntity<Project> {
        val userId = oAuth2User.getAttribute<Long>("sub")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        val user = userRepository.findById(userId).awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony") 
        val project = Project().apply {
            this.user = user
            this.name = name
        }
    
        val savedProject = projectRepository.save(project).awaitSingle()
        return ResponseEntity.ok(savedProject)
    }

    @GetMapping("/{projectId}")
    suspend fun getProject(
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @PathVariable projectId: String
    ): Project {
        val userId = oAuth2User.getAttribute<Long>("sub")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        
        val user = userRepository.findById(userId).awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony") 

        return projectRepository.findByIdAndUser(projectId, user)
            .awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie znaleziony")
    }

  
    @PostMapping("/{projectId}/files")
    suspend fun addFileToProject(
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @PathVariable projectId: String,
        @RequestBody file: FileElement
    ): Project {
        val userId = oAuth2User.getAttribute<Long>("sub")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        
        val user = userRepository.findById(userId).awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony") 
        val project = projectRepository.findByIdAndUser(projectId, user)
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
        val userId = oAuth2User.getAttribute<Long>("sub")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        

        val user = userRepository.findById(userId).awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony") 

        val project = projectRepository.findByIdAndUser(projectId, user)
            .awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie znaleziony")
        
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