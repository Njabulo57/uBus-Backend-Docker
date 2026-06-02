package org.tracker.ubus.ubus.Components.Users.Admin.Service.Interface;

import org.springframework.data.domain.Pageable;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverActivePage;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverActiveResponseDTO;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverPendingResponseDTO;

import java.util.Collection;
import java.util.UUID;

public interface IAdminService {

    Collection<DriverPendingResponseDTO> getPendingDrivers();

    DriverActivePage getActiveDrivers(Pageable pageable);

    boolean approveDriver(UUID driverId);
}
