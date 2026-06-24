package org.tracker.ubus.ubus.Components.Jwt.Filter;



import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.tracker.ubus.ubus.Components.Jwt.JwtService.JwtService;
import org.tracker.ubus.ubus.Components.Shared.FilterHandlers.RequestTokenExtractor;
import org.tracker.ubus.ubus.Components.Shared.FilterHandlers.ResponseWriter;
import org.tracker.ubus.ubus.Components.TokenBlacklist.Service.Impl.BlacklistedTokenService;
import java.io.IOException;


@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final ResponseWriter responseWriter;
    private final RequestTokenExtractor requestTokenExtractor;
    private final BlacklistedTokenService blacklistedTokenService;

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {

        String token = this.requestTokenExtractor.extractTokenFromAuthHeaderRequest(request);

        if(token != null) {
            //check whether token is blacklisted
            if (blacklistedTokenService.isBlacklisted(token)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return;
            }

            try {

                var authToken = this.requestTokenExtractor.validateToken(token);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (ExpiredJwtException e) {
                this.responseWriter.write(response, "Session Expired.", HttpStatus.UNAUTHORIZED);
                return; // rejecting the request from this filter
            } //any other exception will just be handled by spring security
        }
        filterChain.doFilter(request, response);
    }


}
