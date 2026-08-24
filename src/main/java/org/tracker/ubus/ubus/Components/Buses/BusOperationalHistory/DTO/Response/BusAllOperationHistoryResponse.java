package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response;

import java.util.Collection;

public record BusAllOperationHistoryResponse(String busName, Collection<BusOperationHistoryResponse> busOperationHistoryResponses) {
}
