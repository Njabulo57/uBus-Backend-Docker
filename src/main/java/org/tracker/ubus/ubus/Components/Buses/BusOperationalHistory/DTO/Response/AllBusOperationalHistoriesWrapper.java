package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response;

import lombok.Builder;

import java.util.Collection;

@Builder
public record AllBusOperationalHistoriesWrapper(Collection<BusOperationHistoryResponse> busOperationHistoryResponses,
                                                int totalPages, int currentPage, int pageSize, int totalElements) {
}
