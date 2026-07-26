package org.tracker.ubus.ubus.Components.Trips.Trip.Enum;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.tracker.ubus.ubus.Components.Trips.Trip.Exceptions.DestinationNotFoundException;
import org.tracker.ubus.ubus.Components.Trips.Trip.Util.TripMathUtil;

import java.util.stream.Stream;

@Getter
@RequiredArgsConstructor
public enum Destination {

    APK("Auckland Park Kingsway Campus",
            -26.181614, 27.999092, 70,
            new double[]{27.999092, -26.181614}),

    JBS("Johannesburg Business School",
            -26.183036, 28.008553, 95,
            new double[]{28.008553, -26.183036}),

    APB("Auckland Park Bunting Road Campus",
            -26.188306, 28.014347, 70,
            new double[]{28.014347, -26.188306}),

    DFC("Doornfontein Campus",
            -26.194294, 28.056403, 65,
            new double[]{28.056403, -26.194294}),

    SWC("Soweto Campus",
            -26.260356, 27.924478, 55,
            new double[]{27.924478, -26.260356});

    private final String label;
    private final double lat;
    private final double lng;
    private final double radiusMeters;
    private final double[] latLong;


    public boolean isWithinRadius(double lat, double lng) {
        if (!isValidCoordinate(lat, lng))
            return false;

        double distance = TripMathUtil.haverSineDistance(lat, lng, this.lat, this.lng);
        return distance <= radiusMeters;
    }

    public double distanceTo(double lat, double lng) {
        if (!isValidCoordinate(lat, lng))
            return Double.MIN_VALUE;

        return TripMathUtil.haverSineDistance(
                lat, lng,
                this.lat, this.lng
        );
    }

    public static double distanceTo(double lat, double lng, Destination destination) {
        return TripMathUtil.haverSineDistance(
                lat, lng,
                destination.lat,
                destination.lng
        );
    }

    public static Destination fromLabel(String label) {
        return Stream.of(Destination.values())
                .filter(d -> d.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid Destination: " + label));
    }

    public static Destination findDestinationByCoordinates(double lat, double lng) {
        return Stream.of(Destination.values())
                .filter(d -> d.isWithinRadius(lat, lng))
                .findFirst()
                .orElse(null);
    }

    public static Destination findDestinationByCoordinatesOrThrow(double lat, double lng)
            throws DestinationNotFoundException {
        return Stream.of(Destination.values())
                .filter(d -> d.isWithinRadius(lat, lng))
                .findFirst()
                .orElseThrow(() ->
                        new DestinationNotFoundException("Not at a valid campus location"));
    }

    public static Destination findCampusByCoordinates(double lat, double lng) {
        return Stream.of(Destination.values())
                .filter(Destination::isCampus)
                .filter(d -> d.isWithinRadius(lat, lng))
                .findFirst()
                .orElse(null);
    }

    public static Iterable<Destination> getCampusesAsIterable() {
        return Stream.of(Destination.values())
                .filter(Destination::isCampus)
                .toList();
    }



    public boolean isCampus() {
        return this != JBS;
    }

    private boolean isValidCoordinate(double lat, double lng) {
        return lat >= -35 && lat <= -22 && lng >= 16 && lng <= 33;
    }

    @Override
    public String toString() {
        return this.name();
    }
}