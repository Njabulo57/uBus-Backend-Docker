package org.tracker.ubus.ubus.Components.Trip.Enum;

import lombok.Getter;
import org.tracker.ubus.ubus.Components.Trip.Exceptions.TripStatusNotFoundException;

import java.util.stream.Stream;

@Getter
public enum TripRoute {

    APK_APB("APK to APB"),
    APK_DFC("APK to DFC"),
    APK_SWC("APK to SWC"),

    APB_APK("APB to APK"),
    APB_DFC("APB to DFC"),
    APB_SWC("APB to SWC"),

    DFC_APK("DFC to APK"),
    DFC_APB("DFC to APB"),
    DFC_SWC("DFC to SWC"),

    SWC_APK("SWC to APK"),
    SWC_APB("SWC to APB"),
    SWC_DFC("SWC to DFC");

    private final String label;

    TripRoute(String label) {
        this.label = label;
    }

    public static TripRoute fromLabel(String label) {
        return Stream.of(TripRoute.values())
                .filter(t -> t.label.equalsIgnoreCase(label))
                .findFirst()
                .orElseThrow(() -> new TripStatusNotFoundException("Invalid Trip Route.Provoided " + label));

    }
}