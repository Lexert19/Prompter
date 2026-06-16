package com.example.promptengineering.component;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2JwtSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  @Value("${app.frontend.url:http://localhost:8080}")
  private String frontendUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
    String email = oAuth2User.getAttribute("email");
    User user = userRepository.findByEmail(email).orElseThrow();

    String access = jwtTokenProvider.generateAccessToken(user);
    String refresh = jwtTokenProvider.generateRefreshToken(user);

    ResponseCookie accessCookie = ResponseCookie.from("full", access)
        .httpOnly(true).secure(false)
        .path("/").maxAge(900).sameSite("Lax").build();
    ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refresh)
        .httpOnly(true).secure(false)
        .path("/auth/refresh").maxAge(604800).sameSite("Lax").build();

    response.addHeader("Set-Cookie", accessCookie.toString());
    response.addHeader("Set-Cookie", refreshCookie.toString());

    getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/chat");

  }
}
