package com.company.approval.security.jwt;

import com.company.approval.security.principal.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
    private static final String DEFAULT_SECRET = "enterprise-approval-dev-secret-key-change-before-production-2026";

    private final Key signingKey;
    private final long expirationMillis;
    private final String rawSecret;
    private final String environment;

    public JwtTokenService(
            @Value("${security.jwt.secret:" + DEFAULT_SECRET + "}") String secret,
            @Value("${security.jwt.expiration-millis:86400000}") long expirationMillis,
            @Value("${app.environment:development}") String environment) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
        this.rawSecret = secret;
        this.environment = environment == null ? "development" : environment;
    }

    @PostConstruct
    void validateSecret() {
        boolean isDefaultSecret = DEFAULT_SECRET.equals(rawSecret);
        boolean isProductionLike = !"development".equalsIgnoreCase(environment)
                && !"dev".equalsIgnoreCase(environment)
                && !"test".equalsIgnoreCase(environment);
        if (isDefaultSecret && isProductionLike) {
            throw new IllegalStateException(
                    "JWT secret must be overridden via security.jwt.secret or SECURITY_JWT_SECRET when app.environment=" + environment);
        }
        if (isDefaultSecret) {
            log.warn("Using built-in JWT signing secret. Set security.jwt.secret (or env SECURITY_JWT_SECRET) before deploying outside development.");
        }
    }

    public String createToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiresAt = new Date(now.getTime() + expirationMillis);
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .claim("uid", principal.getUserId())
                .claim("displayName", principal.getDisplayName())
                .setIssuedAt(now)
                .setExpiration(expiresAt)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String parseUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}


