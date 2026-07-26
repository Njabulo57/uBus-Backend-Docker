package org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response;


import lombok.Builder;

@Builder
public record DriverBusResponse(String busName, String[] destinations) {
}
