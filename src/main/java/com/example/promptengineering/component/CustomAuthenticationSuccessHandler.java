package com.example.promptengineering.component;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.TwoFactorEmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final TwoFactorEmailService twoFactorService;

    public CustomAuthenticationSuccessHandler(TwoFactorEmailService twoFactorService, UserRepository userRepository) {
        this.twoFactorService = twoFactorService;
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        User user = (User) authentication.getPrincipal();
        boolean hasApiKeys = user.getEncryptedKeys() != null && !user.getEncryptedKeys().isEmpty();

        if (user.isTwoFactorEnabled() && hasApiKeys) {
            request.getSession().setAttribute("2fa_authentication", authentication);
            String sessionId = request.getSession().getId();
            String emailTo = user.getTwoFactorEmail() != null ? user.getTwoFactorEmail() : user.getEmail();
            twoFactorService.createAndSendCode(sessionId, emailTo);
            request.getSession().setAttribute("2fa_user_id", user.getId());
            response.sendRedirect("/auth/2fa");
        } else {
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}
