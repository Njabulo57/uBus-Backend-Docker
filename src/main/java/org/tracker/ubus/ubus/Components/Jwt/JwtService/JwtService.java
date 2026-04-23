package org.tracker.ubus.ubus.Components.Jwt.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.User.Entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
public class JwtService {


    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.duration}")
    private Long duration;

    public String generateToken(UserDetails userDetails, String role) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        SecretKey secretKeyObject = getSecretKey();

        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + duration)) // expanding life-time with the duration
                .issuedAt(new Date(System.currentTimeMillis())) // get the current date
                .claims(claims)
                .subject(userDetails.getUsername())
                .signWith(secretKeyObject)
                .compact(); //this will automatically encode everything to base64
    }


    public String generateToken(User user) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().toString());

        SecretKey secretKeyObject = getSecretKey();

        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + duration)) // expanding life-time with the duration
                .issuedAt(new Date(System.currentTimeMillis())) // get the current date
                .claims(claims)
                .subject(user.getEmail())
                .signWith(secretKeyObject)
                .compact(); //this will automatically encode everything to base64
    }


    /**
     * validates if the token provided is actually valid
     * @param token token passed
     * @param userDetails user's details
     * @return returns if the token is valid or not
     * @throws JwtException throws multiple JwtExceptions including
     * Expired exception and Invalid token exception
     */
    public boolean validateToken(String token, UserDetails userDetails) throws JwtException {

        String username = this.extractUsername(token);
        boolean usernameMatches = Objects.equals(userDetails.getUsername(), username);
        boolean tokenNotExpired = this.isNotExpired(token);
        return usernameMatches && tokenNotExpired;
    }

    public boolean isNotExpired(String token) {
        final Date expirationDate = extractClaim(token, Claims::getExpiration);
        return expirationDate.after(new Date()); // check if the current date is before the expiration
    }

    public String extractUsername(String token) throws JwtException {
        return extractClaim(token, Claims::getSubject);
    }

    private<T> T  extractClaim(String token, Function<Claims, T> claimsResolver) throws JwtException {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) throws JwtException {

        SecretKey secretKeyObject = getSecretKey();
        return Jwts.parser()
                .verifyWith(secretKeyObject)
                .build()
                .parseSignedClaims(token) // this will validate the validity of the token
                .getPayload();
    }

    public SecretKey getSecretKey() {
        byte[] bytes = this.secretKey.getBytes(StandardCharsets.UTF_8);  // Converts to bytes
        return Keys.hmacShaKeyFor(bytes);  // Wraps bytes as a SecretKey object
    }

}
