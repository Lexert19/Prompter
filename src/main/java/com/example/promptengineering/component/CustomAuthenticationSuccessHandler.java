package com.example.promptengineering.component;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.TwoFactorEmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler
        extends
            SavedRequestAwareAuthenticationSuccessHandler {

    private final TwoFactorEmailService twoFactorService;
    private final JwtTokenProvider jwtTokenProvider;
    private final String frontendRedirectUrl;
    private final CsrfTokenRepository csrfRepo = CookieCsrfTokenRepository
            .withHttpOnlyFalse();

    public CustomAuthenticationSuccessHandler(TwoFactorEmailService twoFactorService,
            UserRepository userRepository, JwtTokenProvider jwtTokenProvider,
            @Value("${app.frontend.url}") String frontendRedirectUrl) {
        this.twoFactorService = twoFactorService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.frontendRedirectUrl = frontendRedirectUrl;
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        @NonNull Authentication authentication)
            throws IOException, ServletException {
        if (authentication instanceof OAuth2AuthenticationToken) {
            handleOAuth2Success(authentication, response);
            return;
        }

        User user = (User) authentication.getPrincipal();

        CsrfToken token = csrfRepo.generateToken(request);
        csrfRepo.saveToken(token, request, response);

        assert user != null;
        super.onAuthenticationSuccess(request, response, authentication);

//        if (user.isTwoFactorEnabled()) {
//            request.getSession().setAttribute("2fa_authentication", authentication);
//            String sessionId = request.getSession().getId();
//            String emailTo = user.getTwoFactorEmail() != null
//                    ? user.getTwoFactorEmail()
//                    : user.getEmail();
//            twoFactorService.createAndSendCode(sessionId, emailTo);
//            request.getSession().setAttribute("2fa_user_id", user.getId());
//            response.sendRedirect("/auth/2fa");
//        } else {
//        }
    }

    private void handleOAuth2Success(Authentication authentication,
                                     HttpServletResponse response)
            throws IOException {
        String token = jwtTokenProvider.generateToken(authentication);

        String redirectUrl = frontendRedirectUrl + "/oauth2/redirect?token=" + token;
        response.sendRedirect(redirectUrl);
    }
}
