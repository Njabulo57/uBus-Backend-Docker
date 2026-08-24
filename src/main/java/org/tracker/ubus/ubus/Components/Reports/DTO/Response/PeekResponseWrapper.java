package org.tracker.ubus.ubus.Components.Reports.DTO.Response;

import java.util.Collection;

public record PeekResponseWrapper(Collection<PeekHourResponse> peekHourResponse,
                                  Collection<Integer> peakHours, Collection<MaxPeekHoursInsight> insights) {
}
