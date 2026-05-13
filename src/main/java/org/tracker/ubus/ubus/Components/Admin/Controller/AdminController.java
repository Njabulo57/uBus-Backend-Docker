package org.tracker.ubus.ubus.Components.Admin.Controller;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Admin.DTO.Response.DriverPendingResponseDTO;
import org.tracker.ubus.ubus.Components.Admin.Service.Interface.IAdminService;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admins")
public class AdminController {


    private final IAdminService adminService;


    @GetMapping("/get-pending-drivers")
    public Collection<DriverPendingResponseDTO> findPendingAdmins() {
        return adminService.getPendingDrivers();
    }


    @PostMapping("/approve-driver/{driverId}")
    public boolean approveDriver(@PathVariable UUID driverId) {
        return adminService.approveDriver(driverId);
    }


}
