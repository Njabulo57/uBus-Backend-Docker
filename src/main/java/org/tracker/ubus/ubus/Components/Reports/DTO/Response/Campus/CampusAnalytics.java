package org.tracker.ubus.ubus.Components.Reports.DTO.Response.Campus;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
public class CampusAnalytics {

    private String campus;
    private double averageUtilization;


    private List<PeakTimeWindow> peakTimeWindows;
    private long timesExceededThreshold;
    private long totalTrips;


    private int totalBusesAssigned;
    private int busesNeeded;
    private int busShortage;
    private int excessBuses;

    private String status;

    @Getter
    @Builder
    public static class PeakTimeWindow {
        private LocalTime startTime;
        private LocalTime endTime;
        private double utilization;
        private int tripCount;
    }
}