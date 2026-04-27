package org.tracker.ubus.ubus.Components.Bus.Controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Requests.BusRegisterRequest;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusAdminViewWrapperResponse;
import org.tracker.ubus.ubus.Components.Bus.DTOs.Responses.BusRegisterResponse;
import org.tracker.ubus.ubus.Components.Bus.Service.Interface.IBusService;
import org.tracker.ubus.ubus.Configuration.Security.UserPrincipal;

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


    @GetMapping("/view-buses")
    public BusAdminViewWrapperResponse viewBuses() {
        return busService.viewBuses();
    }


}
