package org.tracker.ubus.ubus.Components.Users.Admin.Controller;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverActivePage;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverActiveResponseDTO;
import org.tracker.ubus.ubus.Components.Users.Admin.DTO.Response.DriverPendingResponseDTO;
import org.tracker.ubus.ubus.Components.Users.Admin.Service.Interface.IAdminService;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admins")
public class AdminController {


    private final IAdminService adminService;


    @GetMapping("/get-pending-drivers")
    public Collection<DriverPendingResponseDTO> findPendingDrivers() {
        return adminService.getPendingDrivers();
    }


    @GetMapping("/get-active-drivers")
    public DriverActivePage findActiveDrivers(@PageableDefault(size = 15) Pageable pageable) {
        return adminService.getActiveDrivers(pageable);
    }

    @PostMapping("/approve-driver/{driverId}")
    public boolean approveDriver(@PathVariable UUID driverId) {
        return adminService.approveDriver(driverId);
    }


}
