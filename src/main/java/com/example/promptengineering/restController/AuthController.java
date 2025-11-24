package com.example.promptengineering.restController;

import com.example.promptengineering.exception.TokenValidationException;
import com.example.promptengineering.exception.UserNotFoundException;
import com.example.promptengineering.service.AuthService;
import com.example.promptengineering.service.ResetTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final ResetTokenService resetTokenService;

    @Autowired
    public AuthController(AuthService authService, ResetTokenService resetTokenService) {
        this.authService = authService;
        this.resetTokenService = resetTokenService;
    }

    @GetMapping("/login")
    public String showLoginForm(Model model) {
        return "login-form";
    }

    @GetMapping("/reset-password-request")
    public String showForgotPasswordForm() {
        return "reset-password-request";
    }

    @GetMapping("/reset-password-info")
    public String resetPasswordInfo() {
        return "reset-password-info";
    }

    @PostMapping("/reset-password-request")
    public String handleForgotPassword(@RequestParam(value = "email", defaultValue = "") String email, Model model) {
        try {
            resetTokenService.createPasswordResetToken(email);
        } catch (UserNotFoundException e) {
            return "redirect:/auth/reset-password-info";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "reset-password-request";
        }
        return "redirect:/auth/reset-password-info";
    }

    @GetMapping("/reset-password-confirm")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "reset-password-confirm";
    }

    @PostMapping("/reset-password-confirm")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String newPassword,
                                      @RequestParam("password_confirmation") String passwordConfirmation,
                                      Model model) {

        if (!newPassword.equals(passwordConfirmation)) {
            model.addAttribute("error", "Passwords do not match.");
            model.addAttribute("token", token);
            return "reset-password-confirm";
        }

        try {
            resetTokenService.resetPassword(token, newPassword);
            return "reset-password-success";
        } catch (TokenValidationException e) {
            model.addAttribute("token", token);
            model.addAttribute("error", e.getMessage());
            return "reset-password-confirm";
        }
    }
}