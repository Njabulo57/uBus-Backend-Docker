package org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Responses;

import lombok.Builder;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;

import java.time.LocalTime;
import java.util.UUID;


@Builder
public record DriverCurrentLocationResponse(UUID tripId, String route,
                                            double latitude,
                                            double longitude,
                                            String distance,
                                            double speed,
                                            String eta,
                                            String busName,
                                            UUID busId,
                                            String driverName,
                                            DelayStatus delay) {

    public static DriverCurrentLocationResponse of(double lat, double lon) {
        return DriverCurrentLocationResponse.builder()
                .latitude(lat)
                .longitude(lon)
                .build();
    }


}
