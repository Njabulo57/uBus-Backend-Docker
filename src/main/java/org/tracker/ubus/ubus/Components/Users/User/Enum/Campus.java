package org.tracker.ubus.ubus.Components.Users.User.Enum;



import lombok.Getter;

import java.util.stream.Stream;

public enum Campus {

    SOWETO("Soweto Campus", -26.2520, -26.2450, 27.8620, 27.8700),
    DOORNFONTEIN("Doornfontein Campus", -26.1960, -26.1890, 28.0340, 28.0420),
    APK("Auckland Park Kingsway Campus", -26.1800, -26.1730, 27.9930, 28.0010),
    APB("Auckland Park Bunting Road Campus", -26.1790, -26.1740, 27.9970, 28.0050);

    @Getter
    private final String label;
    private final double south; // bottom edge (minimum latitude)
    private final double north; // top edge (maximum latitude)
    private final double west;  // left edge (minimum longitude)
    private final double east;  // right edge (maximum longitude)


    Campus(String label, double south, double north, double west, double east) {
        this.label = label;
        this.south = south;
        this.north = north;
        this.west = west;
        this.east = east;
    }



    public boolean isDriverWithinCampus(double lat, double lng) {
        return lat >= south && lat <= north && lng >= west && lng <= east;
    }

    public double getCenterLat() {
        return (south + north) / 2;
    }

    public double getCenterLng() {
        return (west + east) / 2;
    }


    public static Campus isWithinAnyCampusBounds(double lat, double lng) {
        return Stream.of(Campus.values())
                .filter(campus -> campus.isDriverWithinCampus(lat, lng))
                .findFirst()
                .orElse(null);
    }

}
