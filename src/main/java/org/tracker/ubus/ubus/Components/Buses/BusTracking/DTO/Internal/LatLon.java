package org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal;

import lombok.Builder;

@Builder
public record LatLon(double lat, double lon) {

    public static LatLon of(double lon, double lat) {
        return LatLon.builder()
                .lon(lon)
                .lat(lat)
                .build();
    }
}
