package org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests;

import lombok.Builder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Builder
public record DriverCurrentLocationMessage(UUID tripId , double latitude,

                                           double longitude, double speed,
                                           LocalTime eta,
                                           LocalDateTime timePosted, String route, String busName,
                                           boolean isMadeIt, int currentDestIndex) {


    public static DriverCurrentLocationMessage of(double lat, double lon) {
        return DriverCurrentLocationMessage.builder()
                .latitude(lat)
                .longitude(lon)
                .build();

    }
}
