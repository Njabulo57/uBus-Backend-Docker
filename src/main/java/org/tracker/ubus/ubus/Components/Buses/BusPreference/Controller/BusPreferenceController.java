package org.tracker.ubus.ubus.Components.Buses.BusPreference.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Request.BusPreferenceDTO;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceResponse;
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
     * @param busPreferenceDTO the DTO containing the bus route information (Expected newRoute param)
     * @return ResponseEntity containing the created BusPreference entity with:
     *         - id: UUID identifier for the preference
     *         - user: The associated user
     *        route: The Route enum value (DFC_TO_APB_APK, APK_TO_APB_DFC, SWC_TO_APK_APB, APB_TO_APK_SWC,
     *      *           SWC_TO_DFC, DFC_TO_SWC, APK_TO_APB_JBS, JBS_TO_APB_APK)
     */
    @PostMapping("/add")
    public ResponseEntity<BusPreferenceResponse> addBusPreference(@RequestBody BusPreferenceDTO busPreferenceDTO)
    {
        BusPreferenceResponse busPreferenceResponse = busPreferenceService.addPreference(busPreferenceDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(busPreferenceResponse);
    }

    /**
     * Retrieves the current user's bus route preferences.
     *
     * HTTP Method: GET
     * Endpoint: /busPreferences/view
     *
     * @return ResponseEntity containing a list of the user's bus route preferences as string labels
     */
    @GetMapping("/view")
    public ResponseEntity<BusPreferenceResponse> viewPreferences()
    {
        BusPreferenceResponse busPreferenceResponse = busPreferenceService.viewPreferences();
        return ResponseEntity.status(HttpStatus.OK).body(busPreferenceResponse);
    }

    /**
     * Edits an existing bus route preference by replacing the old route with a new one.
     *
     * HTTP Method: PUT
     * Endpoint: /busPreferences/edit
     *
     * @param busPreferenceDTO the DTO containing the old and new bus routes (Expected oldRoute and newRoute params)
     * @return ResponseEntity containing the updated BusPreference entity with:
     *         - id: UUID identifier for the preference
     *         - user: The associated user
     *         - route: The updated Route enum value
     */
    @PutMapping("/edit")
    public ResponseEntity<BusPreferenceResponse> editPreference(@RequestBody BusPreferenceDTO busPreferenceDTO)
    {

        BusPreferenceResponse busPreferenceResponse = busPreferenceService.editPreference(busPreferenceDTO);
        return ResponseEntity.status(HttpStatus.OK).body(busPreferenceResponse);
    }

    /**
     * Deletes an existing bus route preference for the current user.
     *
     * HTTP Method: DELETE
     * Endpoint: /busPreferences/delete
     *
     * @param busPreferenceDTO the DTO containing the bus route to be deleted (Expected oldRoute param)
     * @return ResponseEntity with no content (HTTP 204)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<BusPreference> deletePreference(@RequestBody BusPreferenceDTO busPreferenceDTO)
    {
        busPreferenceService.deletePreference(busPreferenceDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}