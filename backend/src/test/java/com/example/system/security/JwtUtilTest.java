package com.example.system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private static final String SECRET = "test-secret-0123456789abcdef-0123456789abcdef";

    @Test
    @DisplayName("generate then parse keeps uid/username/authorities")
    void roundTripKeepsClaims() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 24);

        String token = jwtUtil.generateToken(42L, "admin", List.of("system:user:list", "system:user:add"));
        Claims claims = jwtUtil.parseToken(token);

        assertAll(
                () -> assertEquals("admin", claims.getSubject()),
                () -> assertEquals(42, ((Number) claims.get("uid")).intValue()),
                () -> assertEquals("system:user:list,system:user:add", claims.get("authorities"))
        );
        assertFalse(jwtUtil.isExpired(token));
    }

    @Test
    @DisplayName("tampered token is rejected")
    void tamperedTokenRejected() {
        JwtUtil jwtUtil = new JwtUtil(SECRET, 24);
        String token = jwtUtil.generateToken(1L, "admin", List.of());

        String tampered = token.substring(0, token.length() - 3) + "xxx";
        assertThrows(JwtException.class, () -> jwtUtil.parseToken(tampered));
    }

    @Test
    @DisplayName("token signed with another secret is rejected")
    void foreignSecretRejected() {
        JwtUtil issuer = new JwtUtil(SECRET, 24);
        JwtUtil verifier = new JwtUtil("another-secret-0123456789abcdef-9876543210", 24);

        String token = issuer.generateToken(1L, "admin", List.of());
        assertThrows(JwtException.class, () -> verifier.parseToken(token));
    }

    @Test
    @DisplayName("negative expire-hours produces an already-expired token")
    void expiredTokenDetected() {
        JwtUtil expiredIssuer = new JwtUtil(SECRET, -1);
        JwtUtil verifier = new JwtUtil(SECRET, 24);

        String token = expiredIssuer.generateToken(1L, "admin", List.of());

        assertTrue(expiredIssuer.isExpired(token));
        assertThrows(ExpiredJwtException.class, () -> verifier.parseToken(token));
    }
}
