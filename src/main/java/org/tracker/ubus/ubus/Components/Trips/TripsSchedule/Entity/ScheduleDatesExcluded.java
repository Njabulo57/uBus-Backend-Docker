package org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.TripsSchedule.Enum.DaysExcludedReason;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDatesExcluded extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, updatable = false)
    private UUID id;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Schedule schedule;

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private DaysExcludedReason reason;

}
