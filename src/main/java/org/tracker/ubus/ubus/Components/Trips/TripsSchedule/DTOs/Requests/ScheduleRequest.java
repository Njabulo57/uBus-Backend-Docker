package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.DTOs.Requests;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDate;
import java.util.UUID;
@Builder
public record ScheduleRequest(UUID id,
                              Route route,
                              LocalDate validFromDate,
                              LocalDate validToDate) {

}
