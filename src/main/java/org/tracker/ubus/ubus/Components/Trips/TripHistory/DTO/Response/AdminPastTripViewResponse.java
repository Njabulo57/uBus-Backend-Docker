package org.tracker.ubus.ubus.Components.Trips.TripHistory.DTO.Response;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import java.util.Collection;


@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public final class AdminPastTripViewResponse extends AbstractPastTrip {

    private int totalPassengers;

    private int totalSeats;

    private int totalStaff;

    private int totalStudents;

    private AdminPastTripBusView bus;

    private Collection<CoOrdinate> coOrdinates;

    private AdminPastTripDriverView driver;
}
