package com.example.promptengineering.config;

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import com.example.promptengineering.repository.UserRepository
import com.example.promptengineering.component.CustomReactiveAuthenticationManager

@Configuration
class ExtendedSecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun reactiveAuthenticationManager(
        userRepository: UserRepository,
        passwordEncoder: PasswordEncoder
    ): ReactiveAuthenticationManager {
        return CustomReactiveAuthenticationManager(userRepository, passwordEncoder)
    }
}