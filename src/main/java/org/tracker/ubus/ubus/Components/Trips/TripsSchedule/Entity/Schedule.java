package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;

import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Enum.ScheduleTripStatus;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;


@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Schedule extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Bus bus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Destination fromDestination;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Route route;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Destination toDestination;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ScheduleTripStatus scheduleTripStatus;

    @Builder.Default
    private boolean isCompleted = false;

    @Column(nullable = false)
    private LocalDate serviceDate;

    @Column(nullable = false)
    private LocalTime departureTime;

    @Column(nullable = false)
    private LocalTime arrivalTime;


}

