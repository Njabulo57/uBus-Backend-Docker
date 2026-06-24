package org.tracker.ubus.ubus.Components.Buses.BusAssignment.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Service.Interface.IBusAssignmentService;

import java.util.UUID;

/**
 * Controller responsible for managing bus assignments.
 * Provides endpoints to facilitate operations related to assigning drivers to buses.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/bus-assignments")
public class BusAssignmentController {


    private IBusAssignmentService busAssignmentService;


    /**
     * Assigns a driver to a bus with a specified schedule.
     *
     * @param driverId the unique identifier of the driver to be assigned
     * @param busId the unique identifier of the bus to which the driver is assigned
     * @param schedule the schedule under which the driver will operate the bus
     * {bus-assignments/assign-driver-to-bus}
     * It takes the driverId, busId, and schedule as path variables.
     * HTTP Method: POST
     * Response Status: 204 NO CONTENT
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping("/assign-driver-to-bus/{driverId}/{busId}/{schedule}")
    public void assignDriverToBus(
            @PathVariable UUID driverId,
            @PathVariable UUID busId,
            @PathVariable String schedule) {
        this.busAssignmentService.assignDriverToBus(driverId, busId, schedule);
    }
}
