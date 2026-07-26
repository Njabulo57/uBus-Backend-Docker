package org.tracker.ubus.ubus.Components.Buses.BusPreference.Listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.BusPreferenceClosestTripResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.ClosetTripInfoResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.UserPrefNearestBusResponse;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.DTO.Response.UserPreferenceNearestBus;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Events.BusPreferenceSuscriberEvent;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;

@Slf4j
@Component
public class BusPreferenceSuscriberEventListener extends AbstractSocketListener<UserPrefNearestBusResponse> {


    public BusPreferenceSuscriberEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/bus-preference-suscriber/");
    }

    @Async
    @EventListener
    public void handleBusPreferenceEvent(BusPreferenceSuscriberEvent event) {

        var user = event.getUser();
        var closestTripInfo = event.getNearestBusByPref();


        var closestTrip = ClosetTripInfoResponse.builder()
                .eta(closestTripInfo.nearestTrip().eta())
                .distance(closestTripInfo.nearestTrip().distance())
                .distanceInKM(closestTripInfo.nearestTrip().distanceInKM())
                .busName(closestTripInfo.nearestTrip().busName())
                .delayStatus(closestTripInfo.nearestTrip().delayStatus())
                .build();


        var response = UserPrefNearestBusResponse.builder()
                .stops(closestTripInfo.stops())
                .from(closestTripInfo.from())
                .to(closestTripInfo.to())
                .isAtUserStop(closestTripInfo.isAtUserStop())
                .totalStopsForJourney(closestTripInfo.totalStopsForJourney())
                .closestTripResponse(closestTrip)
                .build();


        this.sendMessage(user.getEmail(), response);

    }
}
