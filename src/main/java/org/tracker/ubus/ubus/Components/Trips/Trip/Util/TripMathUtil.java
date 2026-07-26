package org.tracker.ubus.ubus.Components.Trips.Trip.Util;

public record TripMathUtil() {

    private static final double EARTH_RADIUS = 6371000.0;

    public static double haverSineDistance(double lat1, double lon1, double lat2, double lon2) {

        //convert degrees to radians and find their differences
        double differenceInLat = Math.toRadians(lat2 - lat1);
        double differenceInLon = Math.toRadians(lon2 - lon1);

        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);

        double sinOfDifferenceInLat = Math.sin(differenceInLat / 2);
        double sinOfDifferenceInLon = Math.sin(differenceInLon / 2);

        double a = Math.pow(sinOfDifferenceInLat, 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.pow(sinOfDifferenceInLon, 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
