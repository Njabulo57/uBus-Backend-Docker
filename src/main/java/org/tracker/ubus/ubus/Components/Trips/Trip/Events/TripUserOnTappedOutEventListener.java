package org.tracker.ubus.ubus.Components.Trips.Trip.Events;


import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tracker.ubus.ubus.Components.Shared.EventHandler.AbstractListeners.AbstractSocketListener;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.TripUserOnTappedOutCardEvent;

@Slf4j
@Component
public class TripUserOnTappedOutEventListener extends AbstractSocketListener<Object> {



    public TripUserOnTappedOutEventListener(SimpMessagingTemplate simpMessagingTemplate) {
        super(simpMessagingTemplate, "/topic/trip-user-on-tapped-out");
    }


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTappedOut(TripUserOnTappedOutCardEvent event) {

        log.info("Received Transactional Trip User On Tapped Out Event");
        log.info("Sending Trip User On Tapped Out Event to Socket on Thread: {}", Thread.currentThread());
        //this.sendMessage("/" + event.getJwtToken(), null);
        log.info("Sent Trip User On Tapped Out Event");
    }
}
