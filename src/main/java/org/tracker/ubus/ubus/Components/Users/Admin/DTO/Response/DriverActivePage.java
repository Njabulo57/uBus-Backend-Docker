package org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response;

import lombok.Builder;

import java.util.Collection;

@Builder
public record DriverActivePage(Collection<DriverActiveResponseDTO> drivers, int totalPages,
                               int totalElements, int pageNumber, int pageSize) {
}
