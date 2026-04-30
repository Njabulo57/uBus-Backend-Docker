package org.tracker.ubus.ubus.Components.Jwt.Filter;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.tracker.ubus.ubus.Components.Jwt.JwtService.JwtService;
import org.tracker.ubus.ubus.Global.Exceptions.ErrorResponse.ErrorResponse;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {

        //getting the authorization from the header
        final String authorization = request.getHeader("Authorization");

        //this means we are dealing with public endpoints so we let them through
        if(authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //the token starts from character 7 from bearer
        final String token = authorization.substring(7);

        try {
            // throws an exception if it fails
            String username = this.jwtService.extractUsername(token);

            //check from their username if they exist and build a security object out of them
            final UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            boolean isTokenValid = this.jwtService.validateToken(token, userDetails);
            if (isTokenValid) {

                var authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        }catch (ExpiredJwtException e) {
            setErrorMessage(response, "Session Expired.", HttpStatus.UNAUTHORIZED);
            return; // rejecting the request from this filter
        } //any other exception will just be handled by spring security

        filterChain.doFilter(request, response);
    }


    private void setErrorMessage(HttpServletResponse response, String message, HttpStatus status)
            throws IOException {

        response.setStatus(status.value());
        LocalDateTime now = LocalDateTime.now();
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .message(message)
                .statusCodePhrase(status.getReasonPhrase())
                .statusCode(status.value())
                .timestamp(now)
                .build();

        objectMapper.writeValue(response.getWriter(), errorResponse);

    }


}
