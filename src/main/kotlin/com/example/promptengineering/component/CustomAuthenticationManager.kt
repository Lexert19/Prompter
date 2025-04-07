package com.example.promptengineering.component

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import com.example.promptengineering.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.stereotype.Component

@Component
open class CustomAuthenticationManager @Autowired constructor(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : AuthenticationManager {


    override fun authenticate(authentication: Authentication): Authentication {
        val username = authentication.name
        val presentedPassword = authentication.credentials.toString()

        val user = userRepository.findByEmail(username)
            .orElseThrow { UsernameNotFoundException("User not found") }

        if (!passwordEncoder.matches(presentedPassword, user.password)) {
            throw BadCredentialsException("Invalid credentials")
        }

        return UsernamePasswordAuthenticationToken(user, presentedPassword, user.authorities)
    }
}