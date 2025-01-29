package com.pegallardo.gateway.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final Duration jwtExpiration;
    private final String tokenHeader;
    private final String tokenPrefix;
    private final String issuer;
    private final String audience;
    private final boolean validationEnabled;
    private final boolean ignoreExpiration;
    private final boolean ignoreSignature;

    public JwtTokenProvider(
            @Value("${spring.security.jwt.secret}") String jwtSecret,
            @Value("${spring.security.jwt.expiration}") Long jwtExpiration,
            @Value("${spring.security.jwt.token-header}") String tokenHeader,
            @Value("${spring.security.jwt.token-prefix}") String tokenPrefix,
            @Value("${spring.security.jwt.issuer}") String issuer,
            @Value("${spring.security.jwt.audience}") String audience,
            @Value("${spring.security.jwt.validation.enabled}") boolean validationEnabled,
            @Value("${spring.security.jwt.validation.ignore-expiration}") boolean ignoreExpiration,
            @Value("${spring.security.jwt.validation.ignore-signature}") boolean ignoreSignature) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtExpiration = Duration.ofSeconds(jwtExpiration);
        this.tokenHeader = tokenHeader;
        this.tokenPrefix = tokenPrefix;
        this.issuer = issuer;
        this.audience = audience;
        this.validationEnabled = validationEnabled;
        this.ignoreExpiration = ignoreExpiration;
        this.ignoreSignature = ignoreSignature;
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Instant now = Instant.now();
        Instant expiry = now.plus(jwtExpiration);

        String token = Jwts.builder()
            .setSubject(userPrincipal.getId().toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiry))
            .setIssuer(issuer)
            .setAudience(audience)
            .signWith(key)
            .compact();
            return tokenHeader + ": " + tokenPrefix + " " + token;
    }

    public Optional<Long> extractUserId(String token) {
        return Optional.ofNullable(Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject())
                .map(Long::parseLong);
    }

    public boolean isTokenValid(String token) {
        if (!validationEnabled) {
            return true;
        }

        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            if (ignoreExpiration && e instanceof ExpiredJwtException) {
                return true;
            }
            if (ignoreSignature && e instanceof SignatureException) {
                return true;
            }
            return false;
        }
    }
}