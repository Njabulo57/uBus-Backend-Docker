package org.tracker.ubus.ubus.Components.Buses.BusAssignment.Mappers;

import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Enum.DriverSchedule;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

@Component
public class BusAssignmentMapper {



    public BusAssignment toEntity(Bus bus, User driver, DriverSchedule driverSchedule) {
        return BusAssignment.builder()
                .bus(bus)
                .driver(driver)
                .driverSchedule(driverSchedule)
                .build();

    }
}
