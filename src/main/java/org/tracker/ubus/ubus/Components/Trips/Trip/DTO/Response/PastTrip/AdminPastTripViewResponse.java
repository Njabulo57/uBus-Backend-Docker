package org.tracker.ubus.ubus.Components.Trips.Trip.DTO.Response.PastTrip;


import lombok.experimental.SuperBuilder;

@SuperBuilder
public final class AdminPastTripViewResponse extends AbstractPastTrip {

    private int totalPassengers;

    private int totalSeats;

    private int totalStaff;

    private int totalStudents;

    private AdminPastTripBusView bus;

    private AdminPastTripDriverView driver;
}
