package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Enum.DayType;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;


@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes={@Index(name = "idx_schedule_leg_schedule_id", columnList = "schedule_id"),
        @Index(name = "idx_schedule_leg_day_of_week", columnList = "day_of_week")})
public class ScheduleLeg extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @JoinColumn(nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Schedule schedule;

    @Column(name = "to_destination", nullable = false)
    @Enumerated(EnumType.STRING)
    private Destination toDestination;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_destination", nullable = false)
    private Destination fromDestination;

    @Column(nullable = false)
    private LocalTime departureTime;


    @Column(nullable = false)
    private LocalTime arrivalTime;

    @OneToMany(mappedBy = "scheduleLeg", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, orphanRemoval = true)
    private Collection<ScheduleLegBusAssignment> busesAssigned = new HashSet<>();
    @Column(nullable = false)

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayOfWeek;

}
