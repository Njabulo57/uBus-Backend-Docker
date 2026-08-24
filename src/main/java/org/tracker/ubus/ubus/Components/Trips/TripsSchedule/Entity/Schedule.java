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
import java.util.Collection;
import java.util.HashSet;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_schedule_route", columnList = "route"),
        })
public class Schedule extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Route route;

    @Column(nullable = false)
    private LocalDate validFromDate;

    @Column(nullable = false)
    private LocalDate validToDate;


    @Builder.Default
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Collection<ScheduleDatesExcluded> datesExcluded = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Collection<ScheduleLeg> scheduleLegs = new HashSet<>();
}

