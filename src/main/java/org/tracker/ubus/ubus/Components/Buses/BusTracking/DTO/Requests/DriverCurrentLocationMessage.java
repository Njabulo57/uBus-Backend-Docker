package org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests;

import java.time.LocalDateTime;
import java.util.UUID;

public record DriverCurrentLocationMessage(UUID tripId , double latitude,
                                           double longitude, double speed,
                                           LocalDateTime timePosted) {
}
