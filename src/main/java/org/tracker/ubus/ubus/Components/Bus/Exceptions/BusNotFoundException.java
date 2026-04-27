package org.tracker.ubus.ubus.Components.Bus.Exceptions;

public final class BusNotFoundException extends IllegalArgumentException {
    public BusNotFoundException(String message) {
        super(message);
    }
}
