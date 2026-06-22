package org.tracker.ubus.ubus.Components.Users.User.Enum;

import lombok.Getter;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Getter
public enum Route {

    //DFC to APB to APK and return (DFC 1-7 buses)
    DFC_TO_APB_APK("Doornfontein to Bunting Road to Kingsway",
            Destination.DOORNFONTEIN, Destination.APB, "DFC-APB-APK"),
    APK_TO_APB_DFC("Kingsway to Bunting Road to Doornfontein",
            Destination.APK, Destination.DOORNFONTEIN, "APK-APB-DFC"),

    //SWC to APK and APB and return (SWC 1-5 buses)
    SWC_TO_APK_APB("Soweto to Kingsway to Bunting Road",
            Destination.SOWETO, Destination.APK, "SWC-APK-APB"),
    APB_TO_APK_SWC("Bunting Road to Kingsway to Soweto",
            Destination.APB, Destination.SOWETO, "APB-APK-SWC"),

    //SWC to DFC (Direct - SWC 1,3 and DFC 2 buses)
    SWC_TO_DFC("Soweto to Doornfontein",
            Destination.SOWETO, Destination.DOORNFONTEIN, "SWC-DFC"),
    DFC_TO_SWC("Doornfontein to Soweto (Direct via Freeway)",
            Destination.DOORNFONTEIN, Destination.SOWETO, "DFC-SWC"),

    //(APK to APB to JBS)
    APK_TO_APB_JBS("Kingsway to Bunting Road to JBS",
            Destination.APK, Destination.JBS, "APK-APB-JBS"),
    JBS_TO_APB_APK("JBS to Bunting Road to Kingsway",
            Destination.JBS, Destination.APK, "JBS-APB-APK");


    private final String label;
    private final Destination fromDestination;
    private final Destination toDestination;
    private final String routeAbbreviated;

    private static final Map<Route, Route> REVERSE_TRIP_MAP;

    static {
        Map<Route, Route> map = new EnumMap<>(Route.class);

        // Route 1: DFC ↔ APB ↔ APK
        map.put(DFC_TO_APB_APK, APK_TO_APB_DFC);
        map.put(APK_TO_APB_DFC, DFC_TO_APB_APK);

        // Route 2: SWC ↔ APK ↔ APB
        map.put(SWC_TO_APK_APB, APB_TO_APK_SWC);
        map.put(APB_TO_APK_SWC, SWC_TO_APK_APB);

        // Route 3: SWC ↔ DFC (Direct)
        map.put(SWC_TO_DFC, DFC_TO_SWC);
        map.put(DFC_TO_SWC, SWC_TO_DFC);

        // JBS Route
        map.put(APK_TO_APB_JBS, JBS_TO_APB_APK);
        map.put(JBS_TO_APB_APK, APK_TO_APB_JBS);

        REVERSE_TRIP_MAP = Collections.unmodifiableMap(map);
    }

    Route(String label, Destination fromDestination, Destination toDestination, String routeAbbreviated) {
        this.label = label;
        this.fromDestination = fromDestination;
        this.toDestination = toDestination;
        this.routeAbbreviated = routeAbbreviated;
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

    public static Route fromAbbreviation(String abbreviation) {
        return Stream.of(Route.values())
                .filter(route -> route.getRouteAbbreviated().equalsIgnoreCase(abbreviation))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such Route. Provided: " + abbreviation));
    }

    public boolean isValidLocation(double userLat, double userLng) {
        return fromDestination.isDriverWithinBounds(userLat, userLng) ||
                toDestination.isDriverWithinBounds(userLat, userLng);
    }

    public Optional<Destination> getCurrentCampus(double userLat, double userLng) {
        if (fromDestination.isDriverWithinBounds(userLat, userLng)) return Optional.of(fromDestination);
        if (toDestination.isDriverWithinBounds(userLat, userLng)) return Optional.of(toDestination);
        return Optional.empty();
    }

    /**
     * Gets the bus route number based on the route type
     */
    public String getRouteNumber() {
        if (this == DFC_TO_APB_APK || this == APK_TO_APB_DFC) {
            return "Route 1";
        } else if (this == SWC_TO_APK_APB || this == APB_TO_APK_SWC) {
            return "Route 2";
        } else if (this == SWC_TO_DFC || this == DFC_TO_SWC) {
            return "Route 3";
        } else if (this == APK_TO_APB_JBS || this == JBS_TO_APB_APK) {
            return "JBS Test Route";
        }
        return "Standard Route";
    }
}