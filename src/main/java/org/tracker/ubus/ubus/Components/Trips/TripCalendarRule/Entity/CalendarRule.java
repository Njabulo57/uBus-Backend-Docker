package org.tracker.ubus.ubus.Components.Trips.TripCalendarRule.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.TripCalendarRule.Enum.CalendarRuleType;

import java.time.LocalDate;
import java.util.UUID;


@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarRule extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    private String name;

    private boolean isRecurring;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalendarRuleType type;

    private LocalDate startDate;

    private LocalDate endDate;

}
