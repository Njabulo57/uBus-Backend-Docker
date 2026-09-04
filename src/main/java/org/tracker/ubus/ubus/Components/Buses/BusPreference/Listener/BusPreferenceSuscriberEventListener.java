package org.tracker.ubus.ubus.Components.Buses.BusPreference.Listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.ClosetTripInfoResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.UserPrefNearestBusResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Events.BusPreferenceSubscriberEvent;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;

@Slf4j
@Component
public class BusPreferenceSuscriberEventListener extends AbstractSocketListener<UserPrefNearestBusResponse> {


    public BusPreferenceSuscriberEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/bus-preference-suscriber/");
    }


    @EventListener
    public void handleBusPreferenceEvent(BusPreferenceSubscriberEvent event) {

        var user = event.getUser();
        var nearestBusByPref = event.getNearestBusByPref();


        var closestTrip = ClosetTripInfoResponse.builder()
                .eta(nearestBusByPref.nearestTrip().getEta())
                .distance(nearestBusByPref.nearestTrip().getDistance())
                .distanceInKM(nearestBusByPref.nearestTrip().getDistanceInKM())
                .busName(nearestBusByPref.nearestTrip().getRegistrationPlate())
                .delayStatus(nearestBusByPref.nearestTrip().getDelayStatus())
                .build();

        var response = UserPrefNearestBusResponse.builder()
                .stops(nearestBusByPref.stops())
                .from("From: " + nearestBusByPref.from() + "  currently from " + nearestBusByPref.from())
                .to("To: " + nearestBusByPref.to()  + " currently going to " + nearestBusByPref.to())
                .isAtUserStop(nearestBusByPref.isAtUserStop())
                .progress(nearestBusByPref.progress())
                .totalStopsForJourney(nearestBusByPref.totalStopsForJourney())
                .closestTripResponse(closestTrip)
                .build();


        this.sendMessage(user.getEmail(), response);

    }
}
