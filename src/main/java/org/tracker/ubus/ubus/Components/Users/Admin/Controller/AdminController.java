package org.tracker.ubus.ubus.Components.Users.Admin.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverActiveResponseDTO;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverPendingResponseDTO;
import org.tracker.ubus.ubus.Components.Users.Admin.Service.Interface.IAdminService;

import java.util.Collection;
import java.util.UUID;

/**
 * Controller for managing administrative operations related to drivers in the system.
 * This controller provides endpoints for retrieving and managing driver data.
 * on /admins
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/admins")
public class AdminController {


    private final IAdminService adminService;


    /**
     * Retrieves a collection of drivers who are currently pending approval.
     * This endpoint allows administrators to view the list of drivers
     * awaiting verification or activation in the system.
     *
     * @return a collection of {@link DriverPendingResponseDTO} objects representing the pending drivers.
     * {admins/getFromTripSimulationCache-pending-drivers}
     */
    @GetMapping("/get-pending-drivers")
    public Collection<DriverPendingResponseDTO> findPendingDrivers() {
        return adminService.getPendingDrivers();
    }



    @GetMapping("/get-active-drivers")
    public Collection<DriverActiveResponseDTO> findActiveDrivers() {
        return adminService.getActiveDrivers();
    }


    /**
     * Approves a driver identified by the specified driver ID.
     * This operation is typically used by administrators to activate or validate
     * a driver's account in the system.
     *
     * @param driverId the unique identifier of the driver to be approved.
     * @return {@code true} if the driver was successfully approved;
     *         {@code false} otherwise.
     * {admins/approve-driver}
     * takes the driverId as path variable.
     * HTTP Method: POST
     */
    @PostMapping("/approve-driver/{driverId}")
    public boolean approveDriver(@PathVariable UUID driverId) {
        return adminService.approveDriver(driverId);
    }


}
