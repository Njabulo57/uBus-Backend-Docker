package org.tracker.ubus.ubus.Components.Reports.DTO.Response;

import lombok.Builder;

@Builder
public record DriverAttendanceResponse(String name, int daysAbsent, int daysPresent, int totalWorkingDays,
                                       int tripsCompleted, int tripsCancelled,
                                       boolean isFlagged, double attendancePercentage) {
}
