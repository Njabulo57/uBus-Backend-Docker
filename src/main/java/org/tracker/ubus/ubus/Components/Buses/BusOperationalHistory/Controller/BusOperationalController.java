package org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Internal.TimeFilterer;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Request.BusConcernRequest;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.AllBusOperationalHistoriesWrapper;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.DTO.Response.BusOperationHistoryResponse;
import org.tracker.ubus.ubus.Components.Buses.BusOperationalHistory.Service.Interface.IBusOperationalService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

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


    @PostMapping("/resolve-concern/{busOperationalId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resolveBusConcern(@PathVariable UUID busOperationalId) {
        this.busOperationalService.resolveIssue(busOperationalId);
    }


    @GetMapping("/get-bus-concerns")
    public AllBusOperationalHistoriesWrapper getBusConcerns(
            @RequestParam final int page,
            @RequestParam final int size,
            @RequestParam final LocalDate from,
            @RequestParam final LocalDate to,
            @RequestParam(required = false, defaultValue = "false") final boolean resolved
            ) {
        var timeFilterer = TimeFilterer.of(from, to);
        var pageRequest = PageRequest.of(page, size);
        return this.busOperationalService.getBusConcerns(timeFilterer, pageRequest, resolved);

    }

}
