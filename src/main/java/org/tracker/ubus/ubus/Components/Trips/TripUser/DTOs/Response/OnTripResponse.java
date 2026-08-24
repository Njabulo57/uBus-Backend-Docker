package org.tracker.ubus.ubus.Components.Trips.TripUser.DTOs.Response;


import lombok.Builder;
import java.util.UUID;

@Builder
public record OnTripResponse(boolean isOnTrip, UUID tripID){

}