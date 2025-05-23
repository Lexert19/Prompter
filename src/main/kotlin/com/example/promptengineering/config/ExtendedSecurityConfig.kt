package com.example.promptengineering.config;

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class ExtendedSecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

//    @Bean
//    fun reactiveAuthenticationManager(
//        userRepository: UserRepository,
//        passwordEncoder: PasswordEncoder
//    ): ReactiveAuthenticationManager {
//        return CustomReactiveAuthenticationManager(userRepository, passwordEncoder)
//    }
}