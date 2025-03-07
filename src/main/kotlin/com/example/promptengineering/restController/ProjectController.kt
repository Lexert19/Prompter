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
import org.springframework.security.core.context.ReactiveSecurityContextHolder
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
        @RequestBody name: String
    ): ResponseEntity<Project> {
        val context = ReactiveSecurityContextHolder.getContext().awaitSingle()
       val oAuth2User = context.authentication?.principal as? OAuth2User
           ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak autentykacji")
        val userId = oAuth2User.getAttribute<String>("sub")
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
        @PathVariable projectId: String
    ): Project {
        val context = ReactiveSecurityContextHolder.getContext().awaitSingle()
       val oAuth2User = context.authentication?.principal as? OAuth2User
           ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak autentykacji")
        val userId = oAuth2User.getAttribute<String>("sub")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        
        val user = userRepository.findById(userId).awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony") 

        return projectRepository.findByIdAndUser(projectId, user)
            .awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie znaleziony")
    }

    @GetMapping
    suspend fun getUserProjects(): ResponseEntity<List<Project>> {
        val context = ReactiveSecurityContextHolder.getContext().awaitSingle()
       val oAuth2User = context.authentication?.principal as? OAuth2User
           ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak autentykacji")
        val userId = oAuth2User.getAttribute<String>("sub")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        
        val user = userRepository.findById(userId).awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony")

        val projectsFlux = projectRepository.findAllByUser(user)  
        val projects = projectsFlux.collectList().awaitSingle() 
      

        return ResponseEntity.ok(projects)
    }

  
    @PostMapping("/{projectId}/files")
    suspend fun addFileToProject(
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @PathVariable projectId: String,
        @RequestBody file: FileElement
    ): Project {
        val userId = oAuth2User.getAttribute<String>("sub")
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Brak identyfikatora użytkownika")
        
        val user = userRepository.findById(userId).awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Użytkownik nie znaleziony") 
        val project = projectRepository.findByIdAndUser(projectId, user)
            .awaitSingle()
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Projekt nie znaleziony")
        
        embeddingService.addFileToProject(project, file, user)
        return projectRepository.save(project).awaitSingle()
    }

    @PostMapping("/{projectId}/similar-fragments")
    suspend fun getSimilarFragments(
        @AuthenticationPrincipal oAuth2User: OAuth2User,
        @PathVariable projectId: String,
        @RequestParam query: String
    ): ResponseEntity<List<String>> {
        val userId = oAuth2User.getAttribute<String>("sub")
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