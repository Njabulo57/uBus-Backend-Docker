package org.tracker.ubus.ubus.Components.Trips.Trip.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.Trips.TripHistory.Entity.TripHistoryPoint;
import org.tracker.ubus.ubus.Components.Trips.TripStop.Entity.TripStop;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Entity.TripUser;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
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
    @Column(nullable = false, updatable = false,  unique = true)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Route route;

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

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<TripUser> tripUsers = new HashSet<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<TripHistoryPoint> tripHistoryPoints = new HashSet<>();
}
