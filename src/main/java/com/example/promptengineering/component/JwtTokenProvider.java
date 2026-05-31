package com.example.promptengineering.component;

import com.example.promptengineering.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration-ms}")
  private long jwtExpirationMs;

  public String generateToken(Authentication authentication) {
    User user = (User) authentication.getPrincipal();
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtExpirationMs);

    return Jwts.builder()
        .subject(user.getId().toString())
        .claim("email", user.getEmail())
        .claim("roles", user.getRoles().stream().map(Enum::name).collect(Collectors.toList()))
        .issuedAt(now)
        .expiration(expiry)
        .signWith(getSignKey())
        .compact();
  }

  public String generateTokenFromUserId(Long userId, String email, List<String> roles) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + jwtExpirationMs);
    return Jwts.builder()
        .subject(userId.toString())
        .claim("email", email)
        .claim("roles", roles)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(getSignKey())
        .compact();
  }

  public Claims getClaims(String token) {
    return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();
  }

  public boolean validateToken(String token) {
    try {
      getClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  private SecretKey getSignKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }
}