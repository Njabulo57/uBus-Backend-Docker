package org.tracker.ubus.ubus.Components.Buses.BusTracking.Mappers;


import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Requests.DriverCurrentLocationMessage;
import org.tracker.ubus.ubus.Components.Buses.BusTracking.DTO.Responses.DriverCurrentLocationResponse;
import org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.DelayStatus;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;


@Component
public class BusTrackingMapper {


    public DriverCurrentLocationResponse toDTO(Trip trip, DriverCurrentLocationMessage location, String eta, DelayStatus delayStatus, Destination from, Destination to) {

        var route = trip.getRoute().getLabel();
        var driverName = formatDriverName(trip.getBusAssignment().getDriver());

        System.err.println("sending for " + trip.getBusAssignment().getBus().getName());
        System.err.println("from " + from + " to " + to + "");
        var speed = roundOff(location.speed(), 2);
        return DriverCurrentLocationResponse.builder()
                .tripId(trip.getId())
                .busName(trip.getBusAssignment().getBus().getName())
                .busId(trip.getBusAssignment().getBus().getId())
                .driverName(driverName)
                .route(route)
                .eta(eta + " " + from + " to " + to)
                .delay(delayStatus)
                .latitude(location.latitude())
                .longitude(location.longitude())
                .speed(speed)
                .build();
    }

    private String formatDriverName(User user) {
        var firstName = user.getFirstname();
        var firstNameInitialCapitalized = Character.toUpperCase(firstName.charAt(0));
        return firstNameInitialCapitalized + ". " + user.getLastname();
    }


    private double roundOff(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }
}
