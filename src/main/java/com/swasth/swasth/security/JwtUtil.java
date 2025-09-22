package com.swasth.swasth.security;


import io.jsonwebtoken.*;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-exp}")
    private long accessExp;

    @Value("${jwt.refresh-token-exp}")
    private long refreshExp;

    public String generateAccessToken(UserDetails user) {
        return Jwts.builder()
                .claims()                       // switch to claims scope
                .subject(user.getUsername())    // non-deprecated
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExp * 1000))
                .and()                          // back to builder level
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    public String generateRefreshToken(UserDetails user) {
        return Jwts.builder()
                .claims()
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExp * 1000))
                .and()
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()                       // 0.12.x entry point
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validate(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
