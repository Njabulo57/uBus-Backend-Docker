package org.tracker.ubus.ubus.Components.Trips.TripTraffic.Mapper;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Entity.Traffic;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Entity.TripTrafficRange;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Enum.TrafficSeverity;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Enum.TrafficType;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TrafficMapper {



    /**
     * doesjt set traffci delay time
     * @param trip
     * @param iconCategory
     * @param occurredAt
     * @param msg
     * @param tomAPIDelayMinutes
     * @param averageSpeed
     * @param distanceFromOriginKm
     * @return
     */
    public Traffic toEntity(Trip trip, String iconCategory, LocalDateTime occurredAt,
                            DriverCurrentLocationMessage msg, int tomAPIDelayMinutes, double averageSpeed, double distanceFromOriginKm) {



        var trafficServerity = this.mapToSeverity(iconCategory);
        var trafficType = this.mapToTrafficType(iconCategory);

        var lat = msg.latitude();
        var lng = msg.longitude();


        return Traffic.builder()
                .trip(trip)
                .trafficType(trafficType)
                .severity(trafficServerity)
                .occurredAt(occurredAt)
                .delayMinutes(tomAPIDelayMinutes)
                .speedAtEncounter(averageSpeed)
                .normalSpeed(msg.speed())
                .distanceFromOriginKm(distanceFromOriginKm)
                .latitude(lat)
                .longitude(lng)
                .trafficDurationMinutes(0)
                .build();
    }


    public TripTrafficRange toEntity(Traffic traffic, DriverCurrentLocationMessage msg) {
        var lat = msg.latitude();
        var lng = msg.longitude();
        return TripTrafficRange.builder()
                .traffic(traffic)
                .latPointRange(lat)
                .lngPointRange(lng)
                .build();
    }

    public TrafficType mapToTrafficType(String iconCategory) {
        return switch (iconCategory) {

            case "accident" -> TrafficType.ACCIDENT;
            case "construction" -> TrafficType.ROADWORKS;
            case "brokenDownVehicle" -> TrafficType.VEHICLE_BREAKDOWN;
            case "weather" -> TrafficType.WEATHER;
            case "event" -> TrafficType.SPECIAL_EVENT;
            case "congestion", "slowTraffic" -> TrafficType.RUSH_HOUR;
            default -> TrafficType.OTHER;
        };
    }

    public TrafficSeverity mapToSeverity(String iconCategory) {
        return switch (iconCategory) {
            case "roadClosed" -> TrafficSeverity.SEVERE;
            case "accident", "congestion" -> TrafficSeverity.HEAVY;
            default -> TrafficSeverity.LIGHT;
        };
    }


}

