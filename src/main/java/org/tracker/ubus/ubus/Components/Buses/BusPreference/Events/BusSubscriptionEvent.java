package org.tracker.ubus.ubus.Components.Buses.BusPreference.Events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class BusSubscriptionEvent extends ApplicationEvent {

    private final int progress;

    public BusSubscriptionEvent(Object source, int progress) {
        super(source);
        this.progress = progress;
    }
}
