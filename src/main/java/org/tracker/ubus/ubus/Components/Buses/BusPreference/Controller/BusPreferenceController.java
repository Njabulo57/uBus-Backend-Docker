package org.tracker.ubus.ubus.Components.Buses.BusPreference.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Service.Interface.IBusPreferenceService;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

/**
 * REST Controller for managing bus route preferences.
 *
 * Handles endpoints for adding, editing, and deleting user bus route preferences.
 * All endpoints are mapped to the /busPreferences base path.
 */
@RestController
@RequestMapping("/busPreferences")
@RequiredArgsConstructor
public class BusPreferenceController {


    @Autowired
    private IBusPreferenceService busPreferenceService;

    /**
     * Adds a new bus route preference for the current user.
     *
     * HTTP Method: POST
     * Endpoint: /busPreferences/add
     *
     * @param strRoute the bus route as a string label (e.g., "Soweto Campus to Doornfontein Campus")
     * @return ResponseEntity containing the created BusPreference entity with:
     *         - id: UUID identifier for the preference
     *         - user: The associated user
     *         - route: The Route enum value (SWC_DFC, DFC_SWC, APK_APB, APB_APK, APB_DFC, DFC_APB, APK_SWC, SWC_APK)
     */
    @PostMapping("/add")
    public ResponseEntity<BusPreference> addBusPreference(@RequestParam String strRoute)
    {
        Route route = Route.valueOf(strRoute);
        BusPreference busPreference = busPreferenceService.addPreference(route);
        return ResponseEntity.status(HttpStatus.CREATED).body(busPreference);
    }

    /**
     * Edits an existing bus route preference by replacing the old route with a new one.
     *
     * HTTP Method: PUT
     * Endpoint: /busPreferences/edit
     *
     * @param strOldRoute the current bus route as a string label to be replaced
     * @param strNewRoute the new bus route as a string label
     * @return ResponseEntity containing the updated BusPreference entity with:
     *         - id: UUID identifier for the preference
     *         - user: The associated user
     *         - route: The updated Route enum value
     */
    @PutMapping("/edit")
    public ResponseEntity<BusPreference> editPreference(@RequestParam String strOldRoute,
                                                        @RequestParam String strNewRoute)
    {
        Route oldRoute = Route.valueOf(strOldRoute);
        Route newRoute = Route.valueOf(strNewRoute);
        BusPreference busPreference = busPreferenceService.editPreference(oldRoute, newRoute);
        return ResponseEntity.status(HttpStatus.OK).body(busPreference);
    }

    /**
     * Deletes an existing bus route preference for the current user.
     *
     * HTTP Method: DELETE
     * Endpoint: /busPreferences/delete
     *
     * @param strOldRoute the bus route as a string label to be deleted
     * @return ResponseEntity with no content (HTTP 204)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<BusPreference> deletePreference(@RequestParam String strOldRoute)
    {
        Route oldRoute = Route.valueOf(strOldRoute);
        busPreferenceService.deletePreference(oldRoute);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}