package org.tracker.ubus.ubus.Components.Buses.Bus.Controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Requests.BusEditRequest;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Requests.BusRegisterRequest;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Responses.BusAdminViewResponse;
import org.tracker.ubus.ubus.Components.Buses.Bus.DTOs.Responses.BusRegisterResponse;
import org.tracker.ubus.ubus.Components.Buses.Bus.Service.Interface.IBusService;

import java.util.List;
import java.util.UUID;

/**
 * This controller provides RESTful endpoints for managing bus-specific operations.
 * It handles bus registration, updating activity status, deletion, editing of bus details, and retrieval of bus data.
 * All operations comply with specified validation constraints.
 *
 * The controller is mapped to the base path "/busses".
 * It uses methods from the {@link IBusService} to perform operations on bus-related data.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/busses")
public class BusController {

    private final IBusService busService;

    /**
     * Registers a new bus in the system.
     *
     * Request body variables:
     * Name: the unique name of the bus.
     * Model: the model of the bus.
     * Type: the bus type. Valid values are "ELECTRIC" or "COMBUSTION".
     * operationalStatus: the operational status of the bus.
     * Valid values are "OPERATIONAL", "MAINTENANCE", or "OUT OF SERVICE".
     * activityStatus: the current activity status of the bus.
     * Valid values are "STATIONERY", "LOADING PASSENGERS", "ON TRIP", or "BREAK".
     * Capacity: the passenger capacity of the bus. Must be a positive number.
     * toRoute: the route assigned to the bus.
     *
     * @param busRegisterRequest the request containing the details of the bus to be registered.
     *                           It must adhere to the validation constraints defined in {@link BusRegisterRequest}.
     * @return a response containing the details of the registered bus, encapsulated in a {@link BusRegisterResponse}.
     * Endpoint: /busses/register
     * HTTP Method: POST
     * Response Code: 201 Created
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public BusRegisterResponse register(@RequestBody @Valid BusRegisterRequest busRegisterRequest) {
        return busService.registerBus(busRegisterRequest);
    }


    /**
     * Updates the activity status of a specific bus.
     *
     * @param busId          the unique identifier of the bus whose activity status is to be updated.
     * @param activityStatus the new activity status for the bus. Valid values include:
     *                       "STATIONERY", "LOADING PASSENGERS", "ON TRIP", or "BREAK".
     * {bus/edit-bus-activity-status}
     * HTTP Method: PUT
     */
    @PutMapping("/edit-bus-activity-status/{busId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editBusActivityStatus(@PathVariable UUID busId, @RequestParam String activityStatus) {
        this.busService.editBusActivityStatus(busId, activityStatus);
    }


    /**
     * Deletes a bus from the system based on the provided unique identifier.
     *
     * @param busId the unique identifier of the bus to be deleted.
     * {bus/delete-bus}
     * takes the busId as path variable.              
     * HTTP Method: DELETE
     * Response Code: 204 No Content
     */
    @DeleteMapping("/delete-bus/{busId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBus(@PathVariable UUID busId) {
        this.busService.deleteBus(busId);
    }


    /**
     * Edits the details of an existing bus in the system.
     *
     * @param busEditRequest the request containing the updated details of the bus.
     *                       It must adhere to the validation constraints defined in {@link BusEditRequest}.
     * @param busId          the unique identifier of the bus to be edited.
     * {bus/edit-bus} 
     * takes the busId as path variable.                     
     * HTTP Method: PUT
     * Response Code: 204 No Content
     */
    @PutMapping("/edit-bus/{busId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editBus(@RequestBody @Valid BusEditRequest busEditRequest,
                        @PathVariable UUID busId) {
        this.busService.editBus(busEditRequest , busId);
    }


    /**
     * Retrieves a list of all buses available within the system.
     *
     * @return a list of bus details, encapsulated in {@link BusAdminViewResponse},
     *         including information such as bus ID, name, registration number, model,
     *         type, capacity, operational and activity statuses, driver, mileage,
     *         and other relevant attributes.
     * {bus/view-buses}
     * HTTP Method: GET
     */
    @GetMapping("/view-buses")
    public List<BusAdminViewResponse> viewBuses() {
        return busService.viewBuses();
    }

}
