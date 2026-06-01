package org.tracker.ubus.ubus.Components.Trips.TripStop.Enum;

import lombok.Getter;

@Getter
public enum TripStopCampus {

    APK(0D, 0D, 0F, "APK"),
    APB(0D, 0D, 0F, "APB"),
    DFC(0D, 0D, 0F, "DFC"),
    SWC(0D, 0D, 0F, "SWC");

    private final String label;
    private final double latitude;
    private final double longitude;
    private final float radiusMeters;

    TripStopCampus(double latitude, double longitude, float radiusMeters, String label) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.radiusMeters = radiusMeters;
        this.label = label;
    }


    public static TripStopCampus fromLabel(String label) {
        return TripStopCampus.valueOf(label.toUpperCase());
    }

    public static boolean containsAndWithin(String label, double latitude, double longitude) {
        var campusStoppedBy =  TripStopCampus.fromLabel(label);
        return campusStoppedBy.isWithinGeoFence(latitude, longitude);
    }


    public boolean isWithinGeoFence(double latitude, double longitude) {

        return false;
    }

}
