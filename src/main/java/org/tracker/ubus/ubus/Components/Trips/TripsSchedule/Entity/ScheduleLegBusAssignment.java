package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;

import java.time.DayOfWeek;
import java.util.UUID;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleLegBusAssignment extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, updatable = false)
    private UUID id;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private ScheduleLeg scheduleLeg;

    @Column(nullable = false)
    private boolean isCompleted;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Bus bus;

}
