package org.tracker.ubus.ubus.Components.Trips.TripLate.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripLate.Enum.TripLateReason;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table
public class TripLate extends TimeAuditableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @JoinColumn(nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Trip trip;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripLateReason reason;

    private String description;
}
