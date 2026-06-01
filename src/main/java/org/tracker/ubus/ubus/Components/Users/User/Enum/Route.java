package org.tracker.ubus.ubus.Components.Users.User.Enum;

import lombok.Getter;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


@Getter
public enum Route {
    SWC_DFC("Soweto Campus to Doornfontein Campus",
            Campus.SOWETO, Campus.DOORNFONTEIN),
    DFC_SWC("Doornfontein Campus to Soweto Campus",
            Campus.DOORNFONTEIN, Campus.SOWETO),
    APK_APB("Auckland Park Kingsway Campus to Auckland Park Bunting Road Campus",
            Campus.APK, Campus.APB),
    APB_APK("Auckland Park Bunting Road Campus to Auckland Park Kingsway Campus",
            Campus.APB, Campus.APK),
    APB_DFC("Auckland Park Bunting Road Campus to Doornfontein Campus",
            Campus.APB, Campus.DOORNFONTEIN),
    DFC_APB("Doornfontein Campus to Auckland Park Bunting Road Campus",
            Campus.DOORNFONTEIN, Campus.APB),
    APK_SWC("Auckland Park Kingsway Campus to Soweto Campus",
            Campus.APK, Campus.SOWETO),
    SWC_APK("Soweto Campus to Auckland Park Kingsway Campus",
            Campus.SOWETO, Campus.APK);


    private final String label;
    private final Campus fromCampus;
    private final Campus toCampus;

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

    Route(String label, Campus fromCampus, Campus toCampus) {
        this.label = label;
        this.fromCampus = fromCampus;
        this.toCampus = toCampus;
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
        return fromCampus.isDriverWithinCampus(userLat, userLng) || toCampus.isDriverWithinCampus(userLat, userLng);
    }


    public Optional<Campus> getCurrentCampus(double userLat, double userLng) {
        if (fromCampus.isDriverWithinCampus(userLat, userLng)) return Optional.of(fromCampus);
        if (toCampus.isDriverWithinCampus(userLat, userLng)) return Optional.of(toCampus);
        return Optional.empty();
    }
}