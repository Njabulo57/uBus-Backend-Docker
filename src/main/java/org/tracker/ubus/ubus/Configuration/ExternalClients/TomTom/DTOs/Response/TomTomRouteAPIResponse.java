package org.tracker.ubus.ubus.Configuration.ExternalClients.TomTom.DTOs.Response;


import java.util.List;

public interface TomTomRouteAPIResponse {



    record TomTomRouteResponse(
            String formatVersion,
            List<Route> routes
    ) {}

    record Route(
            Summary summary,
            List<Leg> legs,
            List<Section> sections
    ) {}

    record Summary(
            int lengthInMeters,
            int travelTimeInSeconds,
            int trafficDelayInSeconds,
            int trafficLengthInMeters,
            String departureTime,
            String arrivalTime
    ) {}

    record Leg(
            Summary summary,
            List<Point> points
    ) {}

    record Point(
            double latitude,
            double longitude
    ) {}

    record Section(
            int startPointIndex,
            int endPointIndex,
            String sectionType,
            String travelMode,
            SectionTraffic traffic
    ) {}

    record SectionTraffic(
            List<String> categories,
            String magnitudeOfDelay,
            int delayInSeconds,
            int effectiveSpeedInKilometersPerHour
    ) {}
}