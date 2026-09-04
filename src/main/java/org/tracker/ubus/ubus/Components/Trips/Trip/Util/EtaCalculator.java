package org.tracker.ubus.ubus.Components.Trips.Trip.Util;





import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.tracker.ubus.ubus.Components.Trips.Trip.Util.TripMathUtil.haverSineDistance;

public record EtaCalculator() {

    private static final double AUCKLAND_PARK_AVG_SPEED = 50; // km/h
    private static final double SWC_APK_AVG_SPEED = 70; //km/h


    public static LocalTime calculateInitialETA(List<double[]> coordinates, Route route) {

        var distance = calculateFullRouteDistance(coordinates);
        var speed = getAverageSpeed(route);

        var time = distance / speed;
        return LocalTime.now().plusSeconds((long) time);
    }


    public static LocalTime calculateETA(List<double[]> coordinates, double speed) {

        double distance = calculateFullRouteDistance(coordinates); // km
        double hours = distance / speed;

        Duration travelTime = Duration.ofSeconds((long) (hours * 3600));
        return LocalTime.now().plus(travelTime);
    }


    public static LocalTime calculateETA(double distanceMeters, double speedKmh) {


        // Convert speed to meters per second
        double speedMps = (speedKmh * 1000) / 3600;

        // Calculate time in seconds
        double timeSeconds = distanceMeters / speedMps;

        // Round to nearest second
        long seconds = Math.round(timeSeconds);

        return LocalTime.now().plusSeconds(seconds);
    }


    public static DelayStatus containsDelay(LocalTime arrivalTime, LocalTime eta) {
        boolean isDelayed = eta.isAfter(arrivalTime);
        long delayedMinutes = 0;
        if(isDelayed)
            delayedMinutes = Duration.between(arrivalTime, eta).toMinutes();

        var arrivalTimeStr = eta.format(DateTimeFormatter.ofPattern("HH:mm a"));
        return new DelayStatus(isDelayed, delayedMinutes, eta, arrivalTimeStr, "");
    }



    private static double calculateFullRouteDistance(List<double[]> coordinates) {

        double totalDistance = 0;

        for(int i = 0; i < coordinates.size() - 1; i++) {

            var currentLat = coordinates.get(i)[1];     // latitude
            var currentLng = coordinates.get(i)[0];     // longitude
            var nextLat = coordinates.get(i + 1)[1];    // next latitude
            var nextLng = coordinates.get(i + 1)[0];    // next longitude

            totalDistance += haverSineDistance(currentLat, currentLng, nextLat , nextLng);
        }

        return totalDistance;
    }

    public static long getSecondsRemaining(LocalTime eta) {
        if (eta == null) {
            return 0L;
        }
        Duration remaining = Duration.between(LocalTime.now(), eta);
        return remaining.isNegative() ? 0L : remaining.getSeconds();
    }

    private static double getAverageSpeed(Route route) {

        return switch (route) {
            case ROUTE_1, ROUTE_2, ROUTE_JBS -> AUCKLAND_PARK_AVG_SPEED;
            case ROUTE_3 -> SWC_APK_AVG_SPEED;
        };
    }
}
