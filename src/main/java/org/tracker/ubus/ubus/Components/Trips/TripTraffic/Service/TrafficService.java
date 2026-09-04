package org.tracker.ubus.ubus.Components.Trips.TripTraffic.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Mapper.TrafficMapper;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Repository.TrafficMonitorRepository;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.TrafficLookUp.TrafficLookUp;
import org.tracker.ubus.ubus.Configuration.ExternalClients.TomTom.DTOs.Response.TomTomRouteAPIResponse;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class TrafficService {

    private final TrafficMapper trafficMapper;
    private final TrafficLookUp trafficLookUp;
    private final TrafficMonitorRepository trafficMonitorRepository;


    public void saveOrUpdateTraffic(Trip trip, LocalDateTime
        withTime, DriverCurrentLocationMessage currentLocation,
                                    double avgSpeed, double remainingDistance,
                                    TomTomRouteAPIResponse.TomTomRouteResponse response) {


            var exists = this.trafficMonitorRepository.existsByTrip(trip);
            if (exists)  {
                var entity = this.trafficMonitorRepository.findByTripOrThrow(trip);
                var tripTrafficEntry = trafficMapper.toEntity(entity, currentLocation);
                entity.addTrafficRange(tripTrafficEntry);
                return;
            }

            var delayMinutes = this.delayInMinutes(response);
            var iconCategory = this.extractIconCategory(response);
            var entity = this.trafficMapper.toEntity(
                    trip,
                    iconCategory,
                    withTime,
                    currentLocation,
                    delayMinutes,
                    avgSpeed,
                    remainingDistance
            );
            this.trafficMonitorRepository.save(entity);
            this.trafficLookUp.addTrafficInfoToCache(trip.getId(), entity);
    }


    public String extractIconCategory(TomTomRouteAPIResponse.TomTomRouteResponse response) {
        if (response == null || response.routes() == null || response.routes().isEmpty())
            return "unknown";


        var sections = response.routes().getFirst()
                .sections();
        if (sections == null || sections.isEmpty())
            return "unknown";


        var traffic = sections.getFirst()
                .traffic();
        if (traffic == null)
            return "unknown";


        var categories = traffic.categories();
        if (categories == null || categories.isEmpty())
            return "unknown";

        return mapToIconCategory(categories.getFirst());
    }

    /**
     * Map TomTom category to iconCategory
     */
    private String mapToIconCategory(String category) {
        if (category == null) return "unknown";

        return switch (category.toLowerCase()) {
            case "roadclosed" -> "roadClosed";
            case "accident" -> "accident";
            case "construction" -> "construction";
            case "brokendownvehicle" -> "brokenDownVehicle";
            case "weather" -> "weather";
            case "event" -> "event";
            case "jam" -> "congestion";
            case "slowtraffic" -> "slowTraffic";
            case "roadworks" -> "roadWorks";
            default -> category.toLowerCase();
        };
    }

    private int delayInMinutes(TomTomRouteAPIResponse.TomTomRouteResponse response) {
        return 0;
    }
}

