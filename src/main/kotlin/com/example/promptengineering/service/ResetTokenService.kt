package com.example.promptengineering.service

import com.example.promptengineering.entity.ResetToken
import com.example.promptengineering.repository.ResetTokenRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
public class ResetTokenService @Autowired constructor(
    private val authService: AuthService,
    private val resetTokenRepository: ResetTokenRepository,
    private val emailService: EmailService
) {
    private val logger = LoggerFactory.getLogger(ResetTokenService::class.java)
    private val TOKEN_EXPIRATION_HOURS = 24

    suspend fun createPasswordResetToken(email: String): String {
        val token = generateUniqueToken()
        val resetToken = ResetToken().apply {
            this.token = token
            userLogin = email
            creationTime = LocalDateTime.now()
            isUsed = false
        }

        val savedToken = resetTokenRepository.save(resetToken).awaitSingle()
        logger.debug("Token saved: {}", savedToken.token)
        try {
            emailService.sendPasswordResetEmail(email, token)
            return token
        } catch (e: Exception) {
            logger.error("Failed to save token", e)
            throw e
        }
    }

    suspend fun validateResetToken(token: String): ResetToken {
        val resetToken = resetTokenRepository.findByToken(token).awaitSingleOrNull()
            ?: throw Exception("Invalid reset token")

        return validateToken(resetToken)
    }

    suspend fun deleteToken(token: String) {
        resetTokenRepository.deleteByToken(token).awaitSingleOrNull()
    }

    private fun generateUniqueToken(): String {
        return UUID.randomUUID().toString()
    }

    private fun validateToken(resetToken: ResetToken): ResetToken {
        if (resetToken.creationTime.plusHours(TOKEN_EXPIRATION_HOURS.toLong()).isBefore(LocalDateTime.now())) {
            throw IllegalArgumentException("Reset token has expired")
        }
        if (resetToken.isUsed) {
            throw IllegalArgumentException("Reset token has already been used")
        }
        return resetToken
    }

    suspend fun resetPassword(token: String, newPassword: String) {
        val resetToken = validateResetToken(token)
        val user = authService.updatePassword(resetToken.userLogin, newPassword).awaitSingle()
        deleteToken(token)
    }
}