package com.stas.user_service.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.security.Key;
import java.util.List;

@Component
public class JwtUtils {

	private static final String SECRET_KEY = "replace_this_with_a_long_secret_key_1234567890_abcdefghijklmnopqrstuvwxyz";    
	private static final long EXPIRATION_TIME = 86400000; // 1 day
	

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    //  Only generation logic remains (no validation)
    public String generateToken(String username, List<String> roles) {
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}