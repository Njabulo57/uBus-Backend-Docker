package org.tracker.ubus.ubus.Components.Bus.DTOs.Responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDateTime;


@Builder
public record BusRegisterResponse(String name, String model,
                                  String type, String operationalStatus,
                                  String activityStatus, int capacity,

                                  @JsonFormat(shape = JsonFormat.Shape.STRING)
                                  LocalDateTime createdAt) {
}
