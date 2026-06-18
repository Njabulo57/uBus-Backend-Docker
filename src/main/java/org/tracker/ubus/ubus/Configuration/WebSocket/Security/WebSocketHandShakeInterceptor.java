package org.tracker.ubus.ubus.Configuration.WebSocket.Security;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.annotation.Nonnull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.tracker.ubus.ubus.Components.Shared.FilterHandlers.RequestTokenExtractor;
import org.tracker.ubus.ubus.Components.Shared.FilterHandlers.ResponseWriter;
import java.util.Map;


/**
 * Intercepts WebSocket handshake requests to perform authentication
 * and validation of incoming requests before establishing the WebSocket connection.
 *
 * This interceptor leverages JWT (JSON Web Token) for verifying the client's authentication
 * token and associating the authenticated user details with the WebSocket session attributes.
 *
 * Implements the {@link HandshakeInterceptor} interface.
 *
 * Dependencies:
 * - {@code JwtService}: Service for validating and handling JSON Web Tokens.
 * - {@code ResponseWriter}: Utility for writing error responses to invalid requests.
 * - {@code UserDetailsService}: Service for retrieving user-related data.
 * - {@code RequestTokenExtractor}: Utility to extract tokens from HTTP requests.
 */
@Component
@RequiredArgsConstructor
public class WebSocketHandShakeInterceptor implements HandshakeInterceptor{

    private final ResponseWriter responseWriter;
    private final RequestTokenExtractor requestTokenExtractor;

    /**
     * Intercepts the WebSocket handshake request to authenticate and validate the
     * incoming client before establishing the WebSocket connection. This method
     * extracts a JWT token from the HTTP request, validates it, and associates
     * user-related details with the WebSocket session attributes.
     *
     * If the token is valid, the method applies the user's authentication details
     * to the session attributes and allows the handshake to proceed. If the token
     * is expired or invalid, an appropriate error response is written to the client
     * and the handshake is rejected.
     *
     * @param request the {@link ServerHttpRequest} containing the WebSocket handshake request details.
     * @param response the {@link ServerHttpResponse} used to send responses to the client.
     * @param wsHandler the {@link WebSocketHandler} handling the WebSocket connection.
     * @param attributes a {@link Map} that can be used to store additional attributes
     *                   to associate with the WebSocket session.
     * @return {@code true} if the handshake is allowed to proceed, {@code false} otherwise.
     * @throws Exception if an error occurs during the processing of the request.
     */
    @Override
    public boolean beforeHandshake(
            @Nonnull ServerHttpRequest request, @Nonnull ServerHttpResponse response,
            @Nonnull WebSocketHandler wsHandler, @Nonnull Map<String, Object> attributes) throws Exception {

        var token = this.requestTokenExtractor.extractTokenFromAuthHeaderRequest(request);

        try {

            var authentication = this.requestTokenExtractor.validateToken(token);
            attributes.put("userPrincipal", authentication.getPrincipal());
            attributes.put("token", token);
            return true;
        }catch (ExpiredJwtException e){
            this.responseWriter.write(response, "Session Expired", HttpStatus.UNAUTHORIZED);
        }
        return false;
    }


    /**
     * Invoked after the WebSocket handshake is completed. This method allows handling of
     * post-handshake logic, such as logging, cleanup, or additional processing based on the
     * outcome of the handshake.
     *
     * @param request the {@link ServerHttpRequest} containing the details of the initial handshake request.
     * @param response the {@link ServerHttpResponse} used to send responses back to the client.
     * @param wsHandler the {@link WebSocketHandler} that will manage the WebSocket session once established.
     * @param exception an {@link Exception} that may have occurred during the handshake process, or {@code null}
     *                  if the handshake was successful.
     */
    @Override
    public void afterHandshake(
            @NonNull ServerHttpRequest request, @Nonnull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler, @Nullable Exception exception) {}
}
