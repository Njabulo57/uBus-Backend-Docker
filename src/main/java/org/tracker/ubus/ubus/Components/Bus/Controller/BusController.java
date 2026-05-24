package org.tracker.ubus.ubus.Components.Bus.Controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Requests.BusEditRequest;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Requests.BusRegisterRequest;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusAdminViewResponse;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusRegisterResponse;
import org.tracker.ubus.ubus.Components.Bus.Service.Interface.IBusService;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/busses")
public class BusController {

    private final IBusService busService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public BusRegisterResponse register(@RequestBody @Valid BusRegisterRequest busRegisterRequest) {
        return busService.registerBus(busRegisterRequest);
    }

    @PutMapping("/edit-bus/{busId}")
    public void editBus(@RequestBody @Valid BusEditRequest busEditRequest,
                        @PathVariable UUID busId) {
        this.busService.editBus(busEditRequest , busId);
    }

    @GetMapping("/view-buses")
    public List<BusAdminViewResponse> viewBuses() {
        return busService.viewBuses();
    }

}
