package org.tracker.ubus.ubus.Components.EventHandler.Publisher;


import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class MultiEvenPublisher {

    private final ApplicationEventPublisher publisher;


    public void publish(ApplicationEvent event) {
        publisher.publishEvent(event);
    }

    public void publish(ApplicationEvent... events) {
        for (ApplicationEvent event : events)
            publisher.publishEvent(event);
    }

    public void publish(Collection<ApplicationEvent> events) {
        events.forEach(publisher::publishEvent);
    }

    @SafeVarargs
    public final void publish(Supplier<ApplicationEvent>... events) {
        for(Supplier<ApplicationEvent> event : events)
            publisher.publishEvent(event.get());
    }
}
