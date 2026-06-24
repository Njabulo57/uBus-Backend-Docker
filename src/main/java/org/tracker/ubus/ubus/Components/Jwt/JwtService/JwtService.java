package org.tracker.ubus.ubus.Components.Jwt.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;


import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Service class responsible for managing JSON Web Tokens (JWTs) for authentication
 * and authorization purposes.
 */
@Service
public class JwtService {


    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.duration}")
    private Long duration;

    /**
     * Generates a JSON Web Token (JWT) for the provided user details and role.
     *
     * @param userDetails the user details containing username information.
     * @param role the role to be included in the token claims.
     * @return a signed JWT as a string containing the user information and claims.
     */
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


    /**
     * Generates a JSON Web Token (JWT) for the specified user and role.
     *
     * @param userDetails the user entity containing the user's email.
     * @param role the role associated with the user to include in the token claims.
     * @return a signed JWT as a string that includes the user's email, role, and other claims.
     */
    public String generateToken(User userDetails, String role) {

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        SecretKey secretKeyObject = getSecretKey();

        return Jwts.builder()
                .expiration(new Date(System.currentTimeMillis() + duration)) // expanding life-time with the duration
                .issuedAt(new Date(System.currentTimeMillis())) // get the current date
                .claims(claims)
                .subject(userDetails.getEmail())
                .signWith(secretKeyObject)
                .compact(); //this will automatically encode everything to base64
    }


    /**
     * Generates a JSON Web Token (JWT) for the specified user.
     * @param user the user entity
     * @return a signed JWT as a string that includes the user's email and role.
     */
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


    /**
     * Extracts all claims from the provided JSON Web Token (JWT).
     * @param token the JWT from which claims are to be extracted.
     * @return a Claims object containing all claims in the JWT.
     * @throws JwtException if the token is invalid or cannot be parsed.
     */
    private Claims extractAllClaims(String token) throws JwtException {

        SecretKey secretKeyObject = getSecretKey();
        return Jwts.parser()
                .verifyWith(secretKeyObject)
                .build()
                .parseSignedClaims(token) // this will validate the validity of the token
                .getPayload();
    }


    /**
     * Generates a SecretKey object from the secret key string.
     * @return a SecretKey object generated from the secret key string.
     */
    private SecretKey getSecretKey() {
        byte[] bytes = this.secretKey.getBytes(StandardCharsets.UTF_8);  // Converts to bytes
        return Keys.hmacShaKeyFor(bytes);  // Wraps bytes as a SecretKey object
    }

    public long getRemainingExpiration(String token) {
        Date expiration = extractClaim(token, Claims::getExpiration);
        return expiration.getTime()-System.currentTimeMillis();
    }


    /**
     * Checks whether the provided JSON Web Token (JWT) has not expired.
     *
     * @param token the JWT to be checked for expiration.
     * @return {@code true} if the token's expiration date is after the current date,
     *         {@code false} otherwise.
     */
    public boolean isNotExpired(String token) {
        final Date expirationDate = extractClaim(token, Claims::getExpiration);
        return expirationDate.after(new Date()); // check if the current date is before the expiration
    }


    /**
     * Extracts the username (subject) from the provided JSON Web Token (JWT).
     *
     * @param token the JWT from which the username is to be extracted.
     * @return the username contained in the token.
     * @throws JwtException if the token is invalid or cannot be parsed.
     */
    public String extractUsername(String token) throws JwtException {
        return extractClaim(token, Claims::getSubject);
    }


    /**
     * Extracts a specific claim from the provided JSON Web Token (JWT) using the given claims resolver function.
     *
     * @param <T> the generic type of the claim to be extracted.
     * @param token the JWT from which the claim is to be extracted.
     * @param claimsResolver a function that resolves the desired claim from the JWT's claims.
     * @return the extracted claim of type T, as resolved by the claimsResolver function.
     * @throws JwtException if the token is invalid or cannot be parsed.
     */
    private<T> T  extractClaim(String token, Function<Claims, T> claimsResolver) throws JwtException {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

}
