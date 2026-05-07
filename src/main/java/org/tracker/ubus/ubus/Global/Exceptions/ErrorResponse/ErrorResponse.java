package org.tracker.ubus.ubus.Global.Exceptions.ErrorResponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(String message, String statusCodePhrase,
                            int statusCode,
                            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
                            LocalDateTime timestamp) {
}
