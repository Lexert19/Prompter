package com.example.promptengineering.component;

import com.example.promptengineering.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
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

        return Jwts.builder().subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("roles",
                        user.getRoles().stream().map(Enum::name)
                                .collect(Collectors.toList()))
                .issuedAt(now).expiration(expiry).signWith(getSignKey()).compact();
    }

    public String generateToken(User user, String scope, long jwtExpirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);
        return Jwts.builder().subject(user.getId().toString())
                .claim("email", user.getEmail()).claim("roles", user.getRoles())
                .claim("scope", scope).issuedAt(now).expiration(expiry)
                .signWith(getSignKey()).compact();
    }

    public String generateAccessToken(User user) {
        return generateToken(user, "full", jwtExpirationMs);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, "refresh", jwtExpirationMs);
    }

    public String generatePreAuthToken(User user) {
        return generateToken(user, "2fa_pending", 10 * 60 * 1000);
    }

    public Claims getClaims(String token) {
        return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, String requiredScope) {
        try {
            Claims claims = getClaims(token);
            if (requiredScope != null
                    && !requiredScope.equals(claims.get("scope", String.class))) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
