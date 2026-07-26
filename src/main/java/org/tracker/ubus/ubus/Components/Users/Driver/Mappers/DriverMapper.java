package org.tracker.ubus.ubus.Components.Users.Driver.Mappers;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Users.Driver.DTO.Response.BusAssignedResponse;

@Component
public class DriverMapper {


    public BusAssignedResponse toDTO(BusAssignment busAssignment) {

        var bus = busAssignment.getBus();
        var busType = bus.getType().getLabel();
        var activityStatus = bus.getActivityStatus().getLabel();
        var schedule = busAssignment.getDriverSchedule().toString();

        var route = bus.getRoute().getLabel();
        var capacity = bus.getCapacity();

        var destinations = formatDestinations(bus);

        return BusAssignedResponse.builder()
                .busName(bus.getName())
                .busModel(bus.getModel())
                .busRegistrationPlate(bus.getRegistrationNumber())
                .busStatus(activityStatus)
                .busRoute(route)
                .destinations(destinations)
                .busType(busType)
                .schedule(schedule)
                .capacity(capacity)
                .build();
    }


    private String formatDestinations(Bus bus) {
        return bus.getRoute().getDestinations()
                .stream()
                .map(Enum::name)
                .reduce((first, second) -> first + ", " + second)
                .orElse("");
    }
}
