package org.tracker.ubus.ubus.Components.Trip.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.BusAssignment.Entity.BusAssignment;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trip.Enum.TripStatus;
import org.tracker.ubus.ubus.Components.User.Enum.Route;
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
}
