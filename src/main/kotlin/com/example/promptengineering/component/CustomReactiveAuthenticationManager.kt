package com.example.promptengineering.component

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import com.example.promptengineering.repository.UserRepository
import reactor.core.publisher.Mono
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono


open class CustomReactiveAuthenticationManager(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return mono {
            val username = authentication.name
            val presentedPassword = authentication.credentials.toString()

            val user = userRepository.findByEmail(username).awaitSingleOrNull()

            if (user == null) {
                return@mono Mono.error<Authentication>(UsernameNotFoundException("User not found")).awaitSingleOrNull()
            }


            if (!passwordEncoder.matches(presentedPassword, user.password)) {
                return@mono Mono.error<Authentication>(BadCredentialsException("Invalid credentials")).awaitSingleOrNull()
            }

            UsernamePasswordAuthenticationToken(
                user.email,
                null,
                user.authorities
            )
        }.onErrorResume { error ->
            Mono.error(error)
        }
    }
}