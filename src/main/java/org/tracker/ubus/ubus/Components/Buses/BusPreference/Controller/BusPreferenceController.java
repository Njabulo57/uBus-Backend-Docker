package org.tracker.ubus.ubus.Components.Buses.BusPreference.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Interface.IBusPreferenceService;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

@RestController
@RequestMapping("/busPreferences")
@RequiredArgsConstructor
public class BusPreferenceController {


    @Autowired
    private IBusPreferenceService busPreferenceService;

    @PostMapping("/add")
    public ResponseEntity<BusPreference> addBusPreference(@RequestParam String strRoute)
    {
        Route route = Route.fromLabel(strRoute);
        BusPreference busPreference = busPreferenceService.addPreference(route);
        return ResponseEntity.status(HttpStatus.CREATED).body(busPreference);
    }

    @PutMapping("/edit")
    public ResponseEntity<BusPreference> editPreference(@RequestParam String strOldRoute,
                                                        @RequestParam String strNewRoute)
    {
        Route oldRoute = Route.fromLabel(strOldRoute);
        Route newRoute = Route.fromLabel(strNewRoute);
        BusPreference busPreference = busPreferenceService.editPreference(oldRoute, newRoute);
        return ResponseEntity.status(HttpStatus.OK).body(busPreference);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<BusPreference> deletePreference(@RequestParam String strOldRoute)
    {
        Route oldRoute = Route.fromLabel(strOldRoute);
        busPreferenceService.deletePreference(oldRoute);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
