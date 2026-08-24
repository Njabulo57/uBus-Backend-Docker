package org.tracker.ubus.ubus.Components.Buses.BusTracking.Mappers;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Internal.BusCurrentNextDestinations;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Responses.DriverCurrentLocationResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Service.Impl.TripLateService;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.Duration;
import java.time.format.DateTimeFormatter;


@Component
public class BusTrackingMapper {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm a");


    public DriverCurrentLocationResponse toDTO(Trip trip, DriverCurrentLocationMessage location, DelayStatus delayStatus, int progress) {

        var route = trip.getRoute().getLabel();
        var driverName = formatDriverName(trip.getBusAssignment().getDriver());

        var formattedEta = this.formatEta(delayStatus);

        var from = trip.getScheduleLegBusAssignment()
                .getScheduleLeg()
                .getFromDestination();

        var to = trip.getScheduleLegBusAssignment()
                .getScheduleLeg()
                .getToDestination();

        var speed = roundOff(location.speed()); //round off the speed to 2 places
        return DriverCurrentLocationResponse.builder()
                .tripId(trip.getId())
                .isFromSim(trip.isFromSimulation())
                .busName(trip.getBusAssignment().getBus().getName())
                .busId(trip.getBusAssignment().getBus().getId())
                .busStatus(trip.getBusAssignment().getBus().getActivityStatus().getLabel())
                .driverName(driverName)
                .route(route)
                .eta(formattedEta + " " + from + " to " + to)
                .delay(delayStatus)
                .latitude(location.latitude())
                .longitude(location.longitude())
                .speed(speed)
                .build();
    }

    public DriverCurrentLocationResponse toRegisteredTripDTO(Trip trip) {

        var busAssignment = trip.getBusAssignment();
        var bus = busAssignment.getBus();
        var driverName = formatDriverName(busAssignment.getDriver());


        var schedule = trip.getScheduleLegBusAssignment()
                .getScheduleLeg();

        var lat =schedule.getFromDestination().getLat();
        var lng = schedule.getFromDestination().getLng();

        return DriverCurrentLocationResponse.builder()
                .tripId(trip.getId())
                .latitude(lat)
                .longitude(lng)
                .speed(0)
                .route(trip.getRoute().getLabel())
                .eta("Loading Passengers")
                .busName(bus.getName())
                .busId(bus.getId())
                .driverName(driverName)
                .build();
    }


    private String formatDriverName(User user) {
        var firstName = user.getFirstname();
        var firstNameInitialCapitalized = Character.toUpperCase(firstName.charAt(0));
        return firstNameInitialCapitalized + ". " + user.getLastname();
    }

    private double roundOff(double value) {
        double scale = Math.pow(10, 2);
        return Math.round(value * scale) / scale;
    }

    private String formatEta(DelayStatus delayStatus) {
        return delayStatus.eta()
                .format(formatter);
    }
}
