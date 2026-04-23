package org.tracker.ubus.ubus.Components.Auth.DTOs.Responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;


import java.time.LocalDateTime;
import java.util.UUID;

@Builder
public record LockedAccountResponse(String message, String statusCodePhrase,
                                    int statusCode, UUID userId,
                                    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
                                    LocalDateTime timestamp) {
}

