package org.tracker.ubus.ubus.Components.Shared.FilterHandlers;



import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Jwt.JwtService.JwtService;

/**
 * The {@code RequestTokenExtractor} class provides functionality for extracting
 * and validating authentication tokens, specifically Bearer tokens, from HTTP requests.
 *
 * This utility class offers methods to:
 * - Extract a Bearer token from the "Authorization" header of an HTTP request.
 * - Validate a JWT token, retrieve user information, and build an authentication object.
 */
@Component
public class RequestTokenExtractor {

    private JwtService jwtService;
    private UserDetailsService userDetailsService;

    public String extractTokenFromAuthHeaderRequest(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer "))
            return authHeader.substring(7);
        return null;
    }


    /**
     * Validates a JWT token, extracts the username from the token, and authenticates
     * the corresponding {@link UserDetails} if the token is valid.
     *
     * @param token the JWT token to be validated.
     * @return an {@link Authentication} object if the token is valid, containing user details
     *         and authorities.
     * @throws ExpiredJwtException if the token has expired.
     * @throws RuntimeException if the token is invalid or the user cannot be authenticated.
     */
    public Authentication validateToken(String token)
        throws ExpiredJwtException {

        String username = jwtService.extractUsername(token);

        //check from their username if they exist and build a security object out of them
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        boolean isTokenValid = jwtService.validateToken(token, userDetails);

        if (isTokenValid)
            return new UsernamePasswordAuthenticationToken(userDetails,
                    null,
                    userDetails.getAuthorities());

        throw new RuntimeException("Invalid Token");
    }


    /**
     * Extracts a Bearer token from the "Authorization" header of the provided HTTP request.
     *
     * @param request the {@link ServerHttpRequest} object containing the HTTP request details.
     * @return the extracted Bearer token as a {@link String}.
     * @throws RuntimeException if the "Authorization" header is missing, incorrectly formatted,
     *                          or does not start with "Bearer".
     */
    public String extractTokenFromAuthHeaderRequest(ServerHttpRequest request) {

        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer "))
            return authHeader.substring(7);
        throw new RuntimeException("Invalid Token");
    }


}
