package org.tracker.ubus.ubus.Components.Trips.Trip.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity.Schedule;
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
public class Trip extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Route route;

    @JoinColumn(updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Schedule schedule; // if this is referenced then its a normal trip, if not its from demand

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TripStatus status;

    @JoinColumn(nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY,  optional = false)
    private BusAssignment busAssignment;

    @Column(nullable = false, updatable = false)
    private int totalCount;

    @Column(updatable = false)
    private LocalDateTime departureTime;

    private LocalDateTime expectedArrivalTime;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private final Set<TripUser> tripUsers = new HashSet<>();


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
