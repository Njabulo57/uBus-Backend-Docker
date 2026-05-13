package org.tracker.ubus.ubus.Components.Admin.Service.Interface;

import org.tracker.ubus.ubus.Components.Admin.DTO.Response.DriverPendingResponseDTO;

import java.util.Collection;
import java.util.UUID;

public interface IAdminService {

    Collection<DriverPendingResponseDTO> getPendingDrivers();


    boolean approveDriver(UUID driverId);
}
