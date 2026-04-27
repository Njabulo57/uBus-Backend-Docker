package org.tracker.ubus.ubus.Components.Bus.Exceptions;

import org.tracker.ubus.ubus.Components.Bus.Entity.Bus;

public final class BusAlreadyExistsException extends IllegalArgumentException {

    private final Bus bus;

    public BusAlreadyExistsException(String message, Bus bus) {
        super(message);
        this.bus = bus;
    }

    @Override
    public String getMessage() {
        if(bus == null)
            return super.getMessage();
        else
            return String.format("Bus with name %s already exists. Bus Details %s", bus.getName(), bus);
    }
}
