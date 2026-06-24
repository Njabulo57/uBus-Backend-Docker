package org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public abstract class EmailEvent extends ApplicationEvent {

    private String header;
    private String body;
    private String toEmail;

    public EmailEvent(Object source) {
        super(source);
    }

    protected abstract String constructHtmlBody();
}
