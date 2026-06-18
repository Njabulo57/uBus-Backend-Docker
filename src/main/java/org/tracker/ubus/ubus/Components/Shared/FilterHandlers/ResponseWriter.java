package org.tracker.ubus.ubus.Components.Shared.FilterHandlers;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.GlobalExceptionHandler.ErrorResponse.ErrorResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * The {@code ResponseWriter} class is a component responsible for writing HTTP responses
 * to clients in a standardized format. It serializes error information into JSON format
 * using an {@link ObjectMapper}.
 *
 * This class is primarily designed to construct and send error responses, including
 * details such as status codes, messages, and timestamps.
 *
 * The response structure adheres to the {@code ErrorResponse} record, which encapsulates
 * fields like a descriptive error message, HTTP status code phrase, numeric status code,
 * and the time the error occurred.
 *
 * Dependencies:
 * - {@link ObjectMapper} is used for JSON serialization of the {@code ErrorResponse}
 *   instance.
 *
 * Functionality:
 * - Constructs an instance of {@code ErrorResponse} containing error details.
 * - Sets the HTTP status code in the response.
 * - Sends the serialized error details as the response body.
 */
@Component
@RequiredArgsConstructor
public class ResponseWriter {

    private final ObjectMapper objectMapper;

    /**
     * Writes an HTTP response with the specified status and message in a standardized format.
     * The response is serialized as a JSON object containing error details.
     *
     * @param response the {@link HttpServletResponse} object used to construct the HTTP response.
     *                 This is where the response status and body will be written.
     * @param status the {@link HttpStatus} representing the HTTP status code to be set for the response.
     *               This includes both the numeric status code and its reason phrase.
     * @param message a {@link String} containing a descriptive message to include in the response body.
     *                This message helps in identifying the nature of the error.
     * @throws IOException if an input or output error occurs while writing the response body.
     */
    public void write(HttpServletResponse response, String message, HttpStatus status) throws IOException {

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

    public void write(ServerHttpResponse response, String message, HttpStatus status) throws IOException {

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        LocalDateTime now = LocalDateTime.now();
        final ErrorResponse errorResponse = ErrorResponse.builder()
                .message(message)
                .statusCodePhrase(status.getReasonPhrase())
                .statusCode(status.value())
                .timestamp(now)
                .build();

        String json = objectMapper.writeValueAsString(errorResponse);
        response.getBody().write(json.getBytes(StandardCharsets.UTF_8));
    }

}
