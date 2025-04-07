package com.example.promptengineering.component

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UsernameNotFoundException
import com.example.promptengineering.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest
import org.springframework.stereotype.Component

@Component
open class CustomAuthenticationManager @Autowired constructor(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userDetailsService: UserDetailsService,
    private val customOAuth2UserService: com.example.promptengineering.service.CustomOAuth2UserService
) : AuthenticationManager {

    private val providerManager: ProviderManager

    init {
        // Create and configure the OAuth2 authentication provider
        val accessTokenResponseClient: OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> =
            DefaultAuthorizationCodeTokenResponseClient()

        val oauth2LoginAuthenticationProvider = OAuth2LoginAuthenticationProvider(
            accessTokenResponseClient,
            customOAuth2UserService
        )

        // Create and configure the username/password authentication provider
        val daoAuthenticationProvider = DaoAuthenticationProvider()
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder)
        daoAuthenticationProvider.setUserDetailsService(userDetailsService)

        // Create a provider manager with both providers
        providerManager = ProviderManager(listOf(
            daoAuthenticationProvider,
            oauth2LoginAuthenticationProvider
        ))
    }

    override fun authenticate(authentication: Authentication): Authentication {
        try {
            return providerManager.authenticate(authentication)
        } catch (e: Exception) {
            println("Authentication error: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}