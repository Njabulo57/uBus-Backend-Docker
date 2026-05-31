package org.tracker.ubus.ubus.Components.Users.User.Enum;

import lombok.Getter;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

public enum Route {
    SWC_DFC("Soweto Campus to Doornfontein Campus", -26.2485, 27.8660, -26.1925, 28.0383),
    DFC_SWC("Doornfontein Campus to Soweto Campus", -26.1925, 28.0383, -26.2485, 27.8660),
    APK_APB("Auckland Park Kingsway Campus to Auckland Park Bunting Road Campus", -26.1769, 27.9973, -26.1765, 28.0010),
    APB_APK("Auckland Park Bunting Road Campus to Auckland Park Kingsway Campus", -26.1765, 28.0010, -26.1769, 27.9973),
    APB_DFC("Auckland Park Bunting Road Campus to Doornfontein Campus", -26.1765, 28.0010, -26.1925, 28.0383),
    DFC_APB("Doornfontein Campus to Auckland Park Bunting Road Campus", -26.1925, 28.0383, -26.1765, 28.0010),
    APK_SWC("Auckland Park Kingsway Campus to Soweto Campus", -26.1769, 27.9973, -26.2485, 27.8660),
    SWC_APK("Soweto Campus to Auckland Park Kingsway Campus", -26.2485, 27.8660, -26.1769, 27.9973);

    @Getter
    private final String label;
    private final double fromLat;
    private final double fromLng;
    private final double toLat;
    private final double toLng;

    private static final Map<Route, Route> REVERSE_TRIP_MAP;

    static {
        Map<Route, Route> map = new EnumMap<>(Route.class);
        map.put(SWC_DFC, DFC_SWC);
        map.put(DFC_SWC, SWC_DFC);
        map.put(APK_APB, APB_APK);
        map.put(APB_APK, APK_APB);
        map.put(APB_DFC, DFC_APB);
        map.put(DFC_APB, APB_DFC);
        map.put(APK_SWC, SWC_APK);
        map.put(SWC_APK, APK_SWC);
        REVERSE_TRIP_MAP = Collections.unmodifiableMap(map);
    }

    Route(String label, double fromLat, double fromLng, double toLat, double toLng) {
        this.label = label;
        this.fromLat = fromLat;
        this.fromLng = fromLng;
        this.toLat = toLat;
        this.toLng = toLng;
    }

    public Route getReverseTrip() {
        return REVERSE_TRIP_MAP.get(this);
    }

    public static Route fromLabel(String label) {
        return Stream.of(Route.values())
                .filter(route -> route.getLabel().equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such Route. Provided: " + label));
    }

    public boolean isValidLocation(double userLat, double userLng) {
        double distanceToStart = distance(userLat, userLng, fromLat, fromLng);
        double distanceToEnd = distance(userLat, userLng, toLat, toLng);
        return distanceToStart <= 500 || distanceToEnd <= 500;
    }

    private double distance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        return 6371000 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }
}