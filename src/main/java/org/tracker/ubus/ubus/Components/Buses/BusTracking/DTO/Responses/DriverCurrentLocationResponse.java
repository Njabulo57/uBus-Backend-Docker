package org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Responses;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;

import java.time.LocalTime;
import java.util.UUID;

@Builder
public record DriverCurrentLocationResponse(UUID tripId, String route,
                                            double latitude,
                                            double longitude,
                                            String distance,
                                            boolean isFromSim,
                                            int progress,
                                            double speed,
                                            String eta,
                                            String busName,
                                            String busStatus,
                                            UUID busId,
                                            String driverName,
                                            DelayStatus delay,
                                            boolean isLate)
{

    public static DriverCurrentLocationResponse of(double lat, double lon) {
        return DriverCurrentLocationResponse.builder()
                .latitude(lat)
                .longitude(lon)
                .build();
    }


}
