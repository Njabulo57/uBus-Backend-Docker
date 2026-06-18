package org.tracker.ubus.ubus.Components.Trips.TripReport.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Campus;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripReport extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @OneToOne
    @JoinColumn(nullable = false, unique = true)
    private Trip trip;

    @JoinColumn(nullable = false, updatable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User generatedBy;

    @Column(nullable = false)
    private Route route;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Campus fromCampus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Campus toCampus;

    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;


    private int totalPassengers;

    private int totalStaff;
    private int totalStudents;
    private int occupancyRate;



    private boolean wasDelayed;
    private int delayMinutes;

    private String delayReason;

}