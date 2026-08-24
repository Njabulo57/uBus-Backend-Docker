package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Request.BusConcernRequest;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.BusOperationHistoryResponse;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Service.Interface.IBusOperationalService;

import java.util.Collection;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bus-operations")
public class BusOperationalController {

    private final IBusOperationalService busOperationalService;

    @GetMapping("/get-most-recent")
    public Collection<BusOperationHistoryResponse> getMostRecentBusOperationHistory() {
        return this.busOperationalService.getMostRecentConcerns();
    }

    @PostMapping("/add-concern")
    public void addBusConcern(@RequestBody @Valid final BusConcernRequest busConcernRequest) {
        this.busOperationalService.addConcern(busConcernRequest);
    }

}
