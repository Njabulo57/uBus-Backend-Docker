package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.PastTrip;


import lombok.Builder;

@Builder
public record AdminPastTripBusView(String busName, String busType) {
}
