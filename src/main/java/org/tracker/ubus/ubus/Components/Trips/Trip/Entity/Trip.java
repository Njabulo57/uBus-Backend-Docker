package org.tracker.ubus.ubus.Components.Trips.Trip.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.ScheduleLegBusAssignment;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {

        @Index(name = "idx_trip_route", columnList = "route"),
        @Index(name = "idx_trip_route_status", columnList = "route, status"),
        @Index(name = "idx_trip_departure_time", columnList = "departure_time"),
        @Index(name = "idx_trip_bus_assignment", columnList = "bus_assignment_id"),
        @Index(name = "idx_trip_departure_status", columnList = "departure_time, status"),
})
public class Trip extends TimeAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Route route;

    @JoinColumn(updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ScheduleLegBusAssignment scheduleLegBusAssignment;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TripStatus status;


    @JoinColumn(nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private BusAssignment busAssignment;

    @Column(nullable = false, updatable = false)
    private int totalCount;

    @Column(updatable = false)
    private LocalDateTime departureTime;

    private LocalDateTime expectedArrivalTime;

    private LocalDateTime actualArrivalTime;


    @Transient
    @Builder.Default
    private boolean isFromSimulation = false;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.PERSIST, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Set<TripUser> tripUsers = new HashSet<>();

    public int countPassengers() {
        return tripUsers.size();
    }

    public void incrementPassengerCount() {
        this.totalCount++;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Trip trip)) return false;
        return Objects.equals(id, trip.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}