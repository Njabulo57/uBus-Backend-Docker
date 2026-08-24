package org.tracker.ubus.ubus.Components.Reports.DTO.Response;

import lombok.Builder;

import java.util.Collection;

@Builder
public record DriverAttendanceWrapper(Collection<DriverAttendanceResponse> responses) {
}
