package com.example.promptengineering.restController;

import com.example.promptengineering.component.EmailRateLimiter;
import com.example.promptengineering.component.JwtTokenProvider;
import com.example.promptengineering.dto.JwtResponse;
import com.example.promptengineering.dto.LoginRequest;
import com.example.promptengineering.dto.RegisterRequest;
import com.example.promptengineering.exception.TokenValidationException;
import com.example.promptengineering.exception.UserAlreadyExistsException;
import com.example.promptengineering.exception.UserNotFoundException;
import com.example.promptengineering.security.IpRateLimiter;
import com.example.promptengineering.service.AuthService;
import com.example.promptengineering.service.ResetTokenService;
import com.example.promptengineering.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final ResetTokenService resetTokenService;
    private final IpRateLimiter rateLimiter;
    private final EmailRateLimiter emailRateLimiter;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService, ResetTokenService resetTokenService,
            IpRateLimiter rateLimiter, EmailRateLimiter emailRateLimiter,
        AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
        UserService userService) {
        this.resetTokenService = resetTokenService;
        this.rateLimiter = rateLimiter;
        this.emailRateLimiter = emailRateLimiter;
      this.authenticationManager = authenticationManager;
      this.tokenProvider = tokenProvider;
      this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request)
        throws UserAlreadyExistsException {
        userService.registerUser(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtResponse(token));
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
    public String handleForgotPassword(@RequestParam(value = "email", defaultValue = "") String email,
                                       Model model, HttpServletRequest request) {
        if (!rateLimiter.isAllowed(request)) {
            model.addAttribute("error", "Zbyt wiele prób. Spróbuj ponownie za chwilę.");
            return "reset-password-request";
        }
        if (!emailRateLimiter.canSend(email)) {
            model.addAttribute("error",
                    "Daily limit reached for this email address. Please try again later.");
            return "reset-password-request";
        }
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
    public String showResetPasswordForm(@RequestParam("token") String token,
                                        Model model) {
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
