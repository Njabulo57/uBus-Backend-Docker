package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.PastTrip;


import lombok.Builder;

@Builder
public record AdminPastTripDriverView(String driverName, String driverPhone) {
}
