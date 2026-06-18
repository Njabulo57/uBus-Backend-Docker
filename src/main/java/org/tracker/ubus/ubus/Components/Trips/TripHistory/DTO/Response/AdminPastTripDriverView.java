package org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response;


import lombok.Builder;

@Builder
public record AdminPastTripDriverView(String driverName, String driverPhone) {
}
