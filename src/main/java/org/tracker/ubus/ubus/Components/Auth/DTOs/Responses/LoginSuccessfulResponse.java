package org.tracker.ubus.ubus.Components.Auth.DTOs.Responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record LoginSuccessfulResponse(String token, String role,
                                      @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
                                      LocalDateTime createdAt) {
}