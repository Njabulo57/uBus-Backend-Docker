package org.tracker.ubus.ubus.Components.Trips.TripTraffic.Enum;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrafficSeverity {
    LIGHT(2, 5),          // 1-5 min delay
    MODERATE(6, 15),       // 5-15 min delay
    HEAVY(16, 30),          // 15-30 min delay
    SEVERE(31, 60),         // 30-60 min delay
    STANDSTILL(61, Integer.MAX_VALUE);   // very bad :|

    private final int delayMin;
    private final int delayMax;


}