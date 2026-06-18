package org.tracker.ubus.ubus.Components.Users.Driver.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Users.Driver.DTO.Response.BusAssignedResponse;
import org.tracker.ubus.ubus.Components.Users.Driver.Service.Interface.IDriverService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/drivers")
public class DriverController {


    private final IDriverService driverService;


    @PostMapping("/has-assigned-bus")
    public boolean containsAssignedBus() {
        return this.driverService.hasAssignedBus();
    }


    @GetMapping("/get-assigned-bus")
    public BusAssignedResponse getDriverAssignedBus() {
        return this.driverService.getDriverAssignedBus();
    }


}
