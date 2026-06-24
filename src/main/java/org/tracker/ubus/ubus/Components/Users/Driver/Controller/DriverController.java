package org.tracker.ubus.ubus.Components.Users.Driver.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tracker.ubus.ubus.Components.Users.Driver.DTO.Response.BusAssignedResponse;
import org.tracker.ubus.ubus.Components.Users.Driver.Service.Interface.IDriverService;

/**
 * DriverController is a REST controller responsible for handling
 * HTTP requests related to driver functionalities.
 * This controller manages endpoints for checking if a driver has an
 * assigned bus and retrieving the details of the assigned bus.
 * on /drivers
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/drivers")
public class DriverController {


    private final IDriverService driverService;


    /**
     * Checks whether there is a bus assigned to the current driver.
     *
     * @return {@code true} if a bus is assigned to the driver, {@code false} otherwise.
     * HTTP Method: POST
     */
    @PostMapping("/has-assigned-bus")
    public boolean containsAssignedBus() {
        return this.driverService.hasAssignedBus();
    }


    /**
     * Retrieves the bus assignment details for the current driver.
     *
     * @return a {@link BusAssignedResponse} object containing details about the assigned bus,
     *         such as bus name, model, registration plate, status, route, schedule,
     *         type, and capacity. If no bus is assigned, the response may contain null or empty values.
     * {drivers/get-assigned-bus}
     */
    @GetMapping("/get-assigned-bus")
    public BusAssignedResponse getDriverAssignedBus() {
        return this.driverService.getDriverAssignedBus();
    }


}
