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
import org.springframework.web.server.ServerWebExchange
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.web.bind.annotation.RequestParam


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
    suspend fun handleForgotPassword(exchange: ServerWebExchange, model: Model): String  {
        val formData = exchange.formData.awaitSingle()
        val email = formData.getFirst("email") ?: "";
        try{
            val token = resetTokenService.createPasswordResetToken(email)
        }catch (e: Exception){
            model.addAttribute("error", e.message)
            return "reset-password-request"
        }
        return "redirect:/auth/reset-password-info"
    }

    @GetMapping("/reset-password-confirm")
    fun showResetPasswordForm(@RequestParam("token") token: String, model: Model): String {
        model.addAttribute("token", token)
        return "reset-password-confirm"
    }

    @PostMapping("/reset-password-confirm")
    fun handleResetPassword(@RequestParam("token") token: String,
                            @RequestParam("password") newPassword: String,
                            @RequestParam("password_confirmation") passwordConfirmation: String,
                            model: Model): String {
        if (newPassword != passwordConfirmation) {
            model.addAttribute("error", "Passwords do not match.")
            model.addAttribute("token", token)
            return "reset-password-confirm"
        }

        return try {
            resetTokenService.resetPassword(token, newPassword)
            "reset-password-success"
        } catch (e: Exception) {
            model.addAttribute("token", token)
            model.addAttribute("error", e.message)
            "reset-password-confirm"
        }
    }
}