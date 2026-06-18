package org.tracker.ubus.ubus.Components.Users.Driver.DTO.Response;


import lombok.Builder;

@Builder
public record BusAssignedResponse(String busName, String busModel,
                                  String busRegistrationPlate, String busStatus,
                                  String busRoute, String schedule,
                                  String busType, int capacity) {
}
