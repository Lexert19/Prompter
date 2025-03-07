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
import com.example.promptengineering.model.ProjectResponse
import kotlin.collections.ArrayList



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
    ): ResponseEntity<ProjectResponse> {
        val userId = oAuth2User.getAttribute<String>("id")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        val user = userRepository.findById(userId).awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony") 
        val project = Project().apply {
            this.user = user
            this.name = name
            this.userId = user.id
        }
    
        val savedProject = projectRepository.save(project).awaitSingle()
        val projectResponse = ProjectResponse(savedProject.id, savedProject.name, ArrayList())
        return ResponseEntity.ok(projectResponse)
    }

    @GetMapping("/{projectId}")
    suspend fun getProject(
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @PathVariable projectId: String
    ): ResponseEntity<ProjectResponse>  {
        val userId = oAuth2User.getAttribute<String>("id")
        ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
    
        val user = userRepository.findById(userId).awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony") 

        val project = projectRepository.findByIdAndUser(projectId, user)
            .awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie znaleziony")
        
        val projectResponse = ProjectResponse(project.id, project.name, project.files)
        return ResponseEntity.ok(projectResponse)
    }
    @GetMapping
    suspend fun getUserProjects(
        @AuthenticationPrincipal oAuth2User: OAuth2User
    ): ResponseEntity<List<ProjectResponse>> {
        val userId = oAuth2User.getAttribute<String>("id")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        
        val user = userRepository.findById(userId).awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony")

        val projectsFlux = projectRepository.findAllByUserId(user.id)
        val projects = projectsFlux.collectList().awaitSingle()
        
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
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @PathVariable projectId: String,
        @RequestBody file: FileElement
    ): ResponseEntity<ProjectResponse> {
        val userId = oAuth2User.getAttribute<String>("id")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        
        val user = userRepository.findById(userId).awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony") 
        val project = projectRepository.findByIdAndUser(projectId, user)
            .awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie znaleziony")
        
        embeddingService.addFileToProject(project, file, user)
        val savedProject = projectRepository.save(project).awaitSingle()
        val projectResponse = ProjectResponse(savedProject.id, savedProject.name, savedProject.files)
        return ResponseEntity.ok(projectResponse)
    }

    @PostMapping("/{projectId}/similar-fragments")
    suspend fun getSimilarFragments(
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @PathVariable projectId: String,
        @RequestParam query: String
    ): ResponseEntity<List<String>> {
        val userId = oAuth2User.getAttribute<String>("id")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        
        val user = userRepository.findById(userId).awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony") 

        val project = projectRepository.findByIdAndUser(projectId, user)
            .awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie znaleziony")
        
        val similarFragments = embeddingService.retrieveSimilarFragments(query, project, user)
        return ResponseEntity.ok(similarFragments)
    }

}