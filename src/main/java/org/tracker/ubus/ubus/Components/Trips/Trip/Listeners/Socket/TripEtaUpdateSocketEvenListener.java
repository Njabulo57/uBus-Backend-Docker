package org.tracker.ubus.ubus.Components.Trips.Trip.Listeners.Socket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Events.TripEtaUpdateEvent;

@Slf4j
@Component
public class TripEtaUpdateSocketEvenListener extends AbstractSocketListener<DelayStatus> {


    public TripEtaUpdateSocketEvenListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/trip-eta-update");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTripEtaUpdateEvent(TripEtaUpdateEvent event) {
        log.info("Received Transactional Trip Eta Update Event");

        log.info("Sending Trip Eta Update Event to Socket on Thread: {}", Thread.currentThread());

        var trip = event.getTrip();
        var delayStatus = event.getDelayStatus();
        this.sendMessage(trip.getId().toString(), delayStatus);

        log.info("Sent Trip Eta Update Event");
    }


}
