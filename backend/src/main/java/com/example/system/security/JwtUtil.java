package com.example.system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expireHours;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expire-hours}") long expireHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireHours = expireHours;
    }

    public String generateToken(Long userId, String username, Collection<String> authorities) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireHours * 3600_000L);
        return Jwts.builder()
                .subject(username)
                .claim("uid", userId)
                .claim("authorities", String.join(",", authorities))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * Parses and verifies the token signature.
     *
     * @throws JwtException            when the token is malformed, forged or unacceptable
     * @throws ExpiredJwtException     when the token is expired
     * @throws IllegalArgumentException when the token is null/blank
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isExpired(String token) {
        try {
            parseToken(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
