package com.example.promptengineering.restController;

import com.example.promptengineering.component.EmailRateLimiter;
import com.example.promptengineering.component.JwtTokenProvider;
import com.example.promptengineering.dto.JwtResponse;
import com.example.promptengineering.dto.LoginRequest;
import com.example.promptengineering.dto.RegisterRequest;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.exception.TokenValidationException;
import com.example.promptengineering.exception.UserAlreadyExistsException;
import com.example.promptengineering.exception.UserNotFoundException;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.security.IpRateLimiter;
import com.example.promptengineering.service.AuthService;
import com.example.promptengineering.service.ResetTokenService;
import com.example.promptengineering.service.TwoFactorEmailService;
import com.example.promptengineering.service.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private final UserRepository userRepository;
    private final TwoFactorEmailService twoFactorService;

    @Autowired
    public AuthController(AuthService authService, ResetTokenService resetTokenService,
            IpRateLimiter rateLimiter, EmailRateLimiter emailRateLimiter,
        AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider,
        UserService userService, UserRepository userRepository, TwoFactorEmailService twoFactorService) {
        this.resetTokenService = resetTokenService;
        this.rateLimiter = rateLimiter;
        this.emailRateLimiter = emailRateLimiter;
      this.authenticationManager = authenticationManager;
      this.tokenProvider = tokenProvider;
      this.userService = userService;
      this.userRepository = userRepository;
      this.twoFactorService = twoFactorService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request)
        throws UserAlreadyExistsException {
        userService.registerUser(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            User user = (User) authentication.getPrincipal();
            boolean hasApiKeys = user.getEncryptedKeys() != null && !user.getEncryptedKeys().isEmpty();

            if (user.isTwoFactorEnabled() && hasApiKeys) {
                String preAuth = tokenProvider.generatePreAuthToken(user);
                String emailTo = user.getTwoFactorEmail() != null ? user.getTwoFactorEmail() : user.getEmail();
                twoFactorService.createAndSendCode(user.getId().toString(), emailTo);
                return ResponseEntity.ok(Map.of("requires2fa", true, "preAuthToken", preAuth));
            }
            return issueTokens(user, response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Nieprawidłowy email lub hasło"));
        }
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verify2fa(@RequestHeader("X-Pre-Auth-Token") String preAuth,
        @RequestParam String code,
        HttpServletResponse response) {
        if (!tokenProvider.validateToken(preAuth, "2fa_pending")) {
            return ResponseEntity.status(401).body(Map.of("error", "Token 2FA wygasł"));
        }
        Claims claims = tokenProvider.getClaims(preAuth);
        Long userId = Long.parseLong(claims.getSubject());

        if (!twoFactorService.verifyCode(userId.toString(), code)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nieprawidłowy kod"));
        }
        User user = userRepository.findById(userId).orElseThrow();
        return issueTokens(user, response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(value = "refresh_token", required = false) String refreshToken,
        HttpServletResponse response) {
        if (refreshToken == null || !tokenProvider.validateToken(refreshToken, "refresh")) {
            return ResponseEntity.status(401).build();
        }
        Claims claims = tokenProvider.getClaims(refreshToken);
        Long userId = Long.parseLong(claims.getSubject());
        User user = userRepository.findById(userId).orElseThrow();
        return issueTokens(user, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie delAccess = ResponseCookie.from("access_token", "")
            .httpOnly(true).path("/").maxAge(0).sameSite("Lax").build();
        ResponseCookie delRefresh = ResponseCookie.from("refresh_token", "")
            .httpOnly(true).path("/auth/refresh").maxAge(0).sameSite("Lax").build();
        response.addHeader(HttpHeaders.SET_COOKIE, delAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, delRefresh.toString());
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<?> issueTokens(User user, HttpServletResponse response) {
        String access = tokenProvider.generateAccessToken(user);
        String refresh = tokenProvider.generateRefreshToken(user);

        ResponseCookie accessCookie = ResponseCookie.from("access_token", access)
            .httpOnly(true)
            .secure(false) // w prod: true
            .path("/")
            .maxAge(Duration.ofMinutes(15))
            .sameSite("Lax")
            .build();
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refresh)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(Duration.ofDays(7))
            .sameSite("Lax")
            .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(new JwtResponse(access));
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
