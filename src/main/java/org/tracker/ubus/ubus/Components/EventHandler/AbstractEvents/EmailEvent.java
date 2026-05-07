package org.tracker.ubus.ubus.Components.EventHandler.AbstractEvents;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public abstract class EmailEvent extends ApplicationEvent {

    private String header;
    private String body;

    public EmailEvent(Object source) {
        super(source);
    }

    public String getHtmlBody() {
        return body;
    }

    protected abstract String constructHtmlBody();
}
