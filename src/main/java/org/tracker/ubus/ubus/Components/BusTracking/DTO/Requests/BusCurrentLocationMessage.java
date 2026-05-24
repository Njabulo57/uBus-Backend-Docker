package org.tracker.ubus.ubus.Components.BusTracking.DTO.Requests;

import java.time.LocalDateTime;
import java.util.UUID;

public record BusCurrentLocationMessage(UUID tripId , double latitude,
                                        double longitude, double speed,
                                        LocalDateTime timePosted) {
}
