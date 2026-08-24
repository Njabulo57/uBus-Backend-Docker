package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Responses;

import lombok.Builder;
import java.util.Collection;

@Builder
public record ScheduleViewWrapper(Collection<ScheduleViewResponse> scheduleViewResponses,
                                  int pageSize, int pageNumber, int totalPages, int totalElements) {


}
