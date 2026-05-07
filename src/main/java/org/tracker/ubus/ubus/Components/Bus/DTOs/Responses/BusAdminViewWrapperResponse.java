package org.tracker.ubus.ubus.Components.Bus.DTOs.Responses;


import lombok.Builder;

import java.util.Collection;

@Builder
public record BusAdminViewWrapperResponse(int totalBuses, int totalOperational,
                                          int totalMaintenance, int totalOutOfService,
                                          int totalElectric, int totalCombustion,
                                          int totalOnDuty, Collection<BusAdminViewResponse> buses) {
}
