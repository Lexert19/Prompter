package com.example.promptengineering.controller

import com.example.promptengineering.service.AuthService
import com.example.promptengineering.service.ResetTokenService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.reactive.result.view.Rendering
import org.springframework.web.server.ServerWebExchange


@Controller
@RequestMapping("/auth")
class AuthController {

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var resetTokenService: ResetTokenService

    @GetMapping("/login")
    fun showLoginForm(model: Model): String {
        return "login-form"
    }

    @GetMapping("/reset-password-request")
    fun showForgotPasswordForm(): String {
        return "reset-password-request"
    }

    @GetMapping("/reset-password-info")
    fun resetPasswordInfo(): String {
        return "reset-password-info"
    }

    @PostMapping("/reset-password-request")
    suspend fun handleForgotPassword(exchange: ServerWebExchange): String  {
        val formData = exchange.formData.awaitSingle()
        val email = formData.getFirst("email")
        try{
            val token = resetTokenService.createPasswordResetToken(email)
        }catch (e: Exception){
            e.printStackTrace()
        }
        return "redirect:/auth/reset-password-info"
    }

    @GetMapping("/reset-password-confirm")
    suspend fun showResetPasswordForm(exchange: ServerWebExchange): Rendering {
        val formData = exchange.formData.awaitSingle()
        val token = formData.getFirst("token")  ?: ""
        return Rendering.view("reset-password")
            .modelAttribute("token", token)
            .build()
    }

    @PostMapping("/reset-password-confirm")
    suspend fun handleResetPassword(exchange: ServerWebExchange, model: Model): String {
        val formData = exchange.formData.awaitSingle()
        val token = formData.getFirst("token")
        val newPassword = formData.getFirst("password")
        val passwordConfirmation = formData.getFirst("password_confirmation")

        if (newPassword != passwordConfirmation) {
            model.addAttribute("error", "Passwords do not match.")
            model.addAttribute("token", token)
            return "reset-password"
        }

        return try {
            resetTokenService.resetPassword(token, newPassword).awaitSingle()
            "reset-password-success"
        } catch (e: Exception) {
            model.addAttribute("token", token)
            model.addAttribute("error", "Invalid or expired token.")
            "reset-password"
        }
    }

    // @ExceptionHandler(Exception::class)
    // fun handleException(ex: Exception, model: Model): String {
    //     model.addAttribute("error", ex.message)
    //     return "error" // Nazwa szablonu Freemarker do wyświetlania błędów
    // }
}