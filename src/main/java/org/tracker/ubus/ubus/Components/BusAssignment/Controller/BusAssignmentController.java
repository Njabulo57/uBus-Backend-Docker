package org.tracker.ubus.ubus.Components.BusAssignment.Controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.BusAssignment.Service.Interface.IBusAssignmentService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bus-assignments")
public class BusAssignmentController {


    private IBusAssignmentService busAssignmentService;


    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/assign-driver-to-bus/{driverId}/{busId}/{schedule}")
    public void assignDriverToBus(
            @PathVariable UUID driverId,
            @PathVariable UUID busId,
            @PathVariable String schedule) {
        this.busAssignmentService.assignDriverToBus(driverId, busId, schedule);
    }
}
