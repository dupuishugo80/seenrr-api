package com.seenrr.seenrr.service;

import java.util.Date;

import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;

@Service
public class JwtService {

    private static String secretKey = "c6f47bd1a9c4f6b8d8d6fdad39f3b128f8fd5f6b47748c9c9d1e6b60c1a9d51f4f035ce1d2e74f71c5b3727e11f015446fa72f8e64ec98181df63f8c9b1c1dfb1";

    public static String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 1000 * 60 * 60);
        
        byte[] keyBytes = secretKey.getBytes();
        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
        
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }


    public static String extractUsername(String token) {
        byte[] keyBytes = secretKey.getBytes();
        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
        
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }


    public static boolean isTokenExpired(String token) {
        byte[] keyBytes = secretKey.getBytes();
        javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
        
        Date expirationDate = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        
        return expirationDate.before(new Date());
    }


    public static boolean isValidToken(String token) {
        try {
            byte[] keyBytes = secretKey.getBytes();
            javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
            
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public static String generateTemporaryToken(String username) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateTemporaryToken'");
    }
}
