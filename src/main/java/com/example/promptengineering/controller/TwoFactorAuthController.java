package com.example.promptengineering.controller;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.security.IpRateLimiter;
import com.example.promptengineering.service.TwoFactorEmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TwoFactorAuthController {

    private final TwoFactorEmailService twoFactorService;
    private final UserRepository userRepository;
    private final IpRateLimiter rateLimiter;

    public TwoFactorAuthController(TwoFactorEmailService twoFactorService, UserRepository userRepository,
            IpRateLimiter rateLimiter) {
        this.twoFactorService = twoFactorService;
        this.userRepository = userRepository;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/auth/2fa")
    public String showTwoFactorForm(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("2fa_user_id");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("error", session.getAttribute("2fa_error"));
        session.removeAttribute("2fa_error");
        return "2fa-form";
    }

    @PostMapping("/auth/2fa")
    public String verifyTwoFactorCode(@RequestParam String code, HttpServletRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("2fa_user_id");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        String sessionId = request.getSession().getId();
        boolean valid = twoFactorService.verifyCode(sessionId, code);
        if (valid) {
            User user = userRepository.findById(userId).orElseThrow();
            Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            session.removeAttribute("2fa_user_id");
            return "redirect:/";
        } else {
            session.setAttribute("2fa_error", "Nieprawidłowy kod. Spróbuj ponownie.");
            return "redirect:/auth/2fa";
        }
    }

    @PostMapping("/auth/2fa/resend")
    public ResponseEntity<?> resendCode(HttpServletRequest request) {
        if (!rateLimiter.isAllowed(request)) {
            return ResponseEntity.status(429).body("Too many requests. Please try again later.");
        }
        HttpSession session = request.getSession();
        Long userId = (Long) session.getAttribute("2fa_user_id");
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }
        User user = userRepository.findById(userId).orElseThrow();
        String emailTo = user.getTwoFactorEmail() != null ? user.getTwoFactorEmail() : user.getEmail();
        String sessionId = request.getSession().getId();
        twoFactorService.createAndSendCode(sessionId, emailTo);
        return ResponseEntity.ok().build();
    }
}
