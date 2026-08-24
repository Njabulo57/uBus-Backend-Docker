package org.tracker.ubus.ubus.Components.Reports.DTO.Response;

import lombok.Builder;

@Builder
public record PeekHourResponse(String hour, int utilizationPercentage) {
}
