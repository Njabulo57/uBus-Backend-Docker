package org.tracker.ubus.ubus.Components.Trips.TripTraffic.Enum;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrafficType {
    ACCIDENT("Accident"),
    ROADWORKS("RoadWorks"),
    RUSH_HOUR("Rush Hour"),
    SPECIAL_EVENT("Special Event"),      // concerts, sports, protests
    WEATHER("Weather"),            // rain, fog, etc.
    VEHICLE_BREAKDOWN("Vehicle Breakdown"),
    TRAFFIC_LIGHT_OUT("Traffic Light Out"),
    PEDESTRIAN_ACTIVITY("Pedestrian Activity"),
    CONSTRUCTION( "Construction"),
    OTHER("Other");

    private final String label;
}