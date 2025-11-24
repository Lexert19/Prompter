package com.example.promptengineering.service;

import com.example.promptengineering.entity.ResetToken;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.exception.TokenValidationException;
import com.example.promptengineering.exception.UserNotFoundException;
import com.example.promptengineering.repository.ResetTokenRepository;
import com.example.promptengineering.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class ResetTokenService {

    private final AuthService authService;
    private final ResetTokenRepository resetTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(ResetTokenService.class);
    private static final long TOKEN_EXPIRATION_HOURS = 24;

    @Autowired
    public ResetTokenService(AuthService authService,
                             ResetTokenRepository resetTokenRepository, UserRepository userRepository,
                             EmailService emailService) {
        this.authService = authService;
        this.resetTokenRepository = resetTokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public String createPasswordResetToken(String email) throws UserNotFoundException {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isEmpty()){
            throw new UserNotFoundException("User not found");
        }

        String token = generateUniqueToken();
        ResetToken resetToken = new ResetToken();
        resetToken.setToken(token);
        resetToken.setUserLogin(email);
        resetToken.setUser(userOptional.get());
        resetToken.setCreationTime(LocalDateTime.now());
        resetToken.setUsed(false);

        ResetToken savedToken = resetTokenRepository.save(resetToken);
        logger.debug("Token saved: {}", savedToken.getToken());
        try {
            emailService.sendPasswordResetEmail(email, token);
            return token;
        } catch (Exception e) {
            logger.error("Failed to save token", e);
            throw e;
        }
    }

    public ResetToken validateResetToken(String token) {
        ResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenValidationException("Token does not exist"));

        if (resetToken.getCreationTime().plusHours(TOKEN_EXPIRATION_HOURS).isBefore(LocalDateTime.now())) {
            throw new TokenValidationException("Token has expired");
        }

        if (resetToken.isUsed()) {
            throw new TokenValidationException("Token has already been used");
        }
        return resetToken;
    }

    private String generateUniqueToken() {
        return UUID.randomUUID().toString();
    }

    public void resetPassword(String token, String newPassword) throws TokenValidationException {
        ResetToken resetToken = validateResetToken(token);
        authService.updatePassword(resetToken.getUser(), newPassword);
        resetTokenRepository.delete(resetToken);
    }
}