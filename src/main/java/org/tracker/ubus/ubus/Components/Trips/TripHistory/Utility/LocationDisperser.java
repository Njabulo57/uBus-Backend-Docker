package org.tracker.ubus.ubus.Components.Trips.TripHistory.Utility;

import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Entity.TripHistoryPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record LocationDisperser() {

    // maximum distance in meters a point can deviate from the simplified line
    // points further than this will be kept, points closer will be removed
    private static final double TOLERANCE_METERS = 100.0;

    /**
     * applies douglas peucker algorithm to reduce the number of coordinates
     * while preserving the overall shape of the bus route
     *
     * @param locations set of GPS points from a single trip
     * @return simplified list of points (much smaller than the original)
     */
    public static Collection<TripHistoryPoint> disperse(List<TripHistoryPoint> locations) {
        return disperse(locations, TOLERANCE_METERS);
    }

    private static Collection<TripHistoryPoint> disperse(List<TripHistoryPoint> locations, double tolerance) {

        // if 2 points or fewer, nothing to simplify
        if(locations == null || locations.size() < 3) return locations;

        // result will hold the simplified points
        List<TripHistoryPoint> result = new ArrayList<>();

        // start recursion with first and last points
        douglasPeucker(locations, 0, locations.size() - 1, result, tolerance);

        return result;
    }

    /**
     * recursive douglas-peucker algorithm
     * finds the point farthest from the line between start and end
     * if that point is within tolerance, keep start and end only
     * otherwise split the line at that point and process both halves
     *
     * @param locations original list of all points
     * @param startIndex index of starting point
     * @param endIndex index of ending point
     * @param result list that accumulates the simplified points
     */
    private static void douglasPeucker(List<TripHistoryPoint> locations,
                                       int startIndex,
                                       int endIndex,
                                       List<TripHistoryPoint> result, double tolerance) {

        // base case: invalid range
        if (startIndex > endIndex) {
            return;
        }

        // find the point with the maximum distance from the line
        double maxDistance = 0;
        int maxIndex = startIndex;

        for (int i = startIndex + 1; i < endIndex; i++) {
            double distance = perpendicularDistance(
                    locations.get(i),           // point to check
                    locations.get(startIndex),  // line start
                    locations.get(endIndex)     // line end
            );
            if (distance > maxDistance) {
                maxDistance = distance;
                maxIndex = i;
            }
        }

        // if the farthest point is outside our tolerance, we need to keep it
        if (maxDistance > tolerance) {
            // recursively process left half (start to farthest point)
            douglasPeucker(locations, startIndex, maxIndex, result, tolerance);
            // recursively process right half (farthest point to end)
            douglasPeucker(locations, maxIndex, endIndex, result, tolerance);
        } else {
            // all points between start and end are within tolerance
            // so we only keep the start and end points

            // add start point if result is empty (first point of the trip)
            if (result.isEmpty()) {
                result.add(locations.get(startIndex));
            }
            // add end point
            result.add(locations.get(endIndex));
        }
    }

    /**
     * calculates the perpendicular distance from a point to a line
     * uses the cross-track distance formula (haversine) for accurate GPS coordinates
     *
     * @param point the point to measure distance from
     * @param lineStart start point of the line
     * @param lineEnd end point of the line
     * @return distance in meters from point to the line
     */
    private static double perpendicularDistance(TripHistoryPoint point,
                                                TripHistoryPoint lineStart,
                                                TripHistoryPoint lineEnd) {

        // extract coordinates from entities
        double lat1 = lineStart.getLatitude();
        double lon1 = lineStart.getLongitude();
        double lat2 = lineEnd.getLatitude();
        double lon2 = lineEnd.getLongitude();
        double latP = point.getLatitude();
        double lonP = point.getLongitude();

        // convert to radians for trigonometric calculations
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        double latPRad = Math.toRadians(latP);
        double lonPRad = Math.toRadians(lonP);

        // difference in longitude between line start and end
        double deltaLon = lon2Rad - lon1Rad;

        // calculate bearing from start point to end point
        double bearing = Math.atan2(
                Math.sin(deltaLon) * Math.cos(lat2Rad),
                Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                        Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLon)
        );

        // calculate angular distance from start point to our test point
        double angularDistance = Math.acos(
                Math.sin(lat1Rad) * Math.sin(latPRad) +
                        Math.cos(lat1Rad) * Math.cos(latPRad) * Math.cos(lonPRad - lon1Rad)
        );

        // cross track distance formula - how far the point is from the great circle path
        double crossTrackDistance = Math.asin(
                Math.sin(angularDistance) * Math.sin(
                        bearing - Math.atan2(
                                Math.sin(lonPRad - lon1Rad) * Math.cos(latPRad),
                                Math.cos(lat1Rad) * Math.sin(latPRad) -
                                        Math.sin(lat1Rad) * Math.cos(latPRad) * Math.cos(lonPRad - lon1Rad)
                        )
                )
        );

        // convert from radians to meters (Earth radius = 6,371,000 meters)
        return Math.abs(crossTrackDistance) * 6371000;
    }
}