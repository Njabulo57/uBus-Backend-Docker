package org.tracker.ubus.ubus.Components.Trips.TripTraffic.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Enum.TrafficSeverity;
import org.tracker.ubus.ubus.Components.Trips.TripTraffic.Enum.TrafficType;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "idx_traffic_trip_id", columnList = "trip_id"),
        @Index(name = "idx_traffic_occurred_at", columnList = "occurred_at")
})
public class Traffic extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // The trip that encountered traffic
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, updatable = false, unique = true)
    private Trip trip;

    // Type of traffic encountered
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TrafficType trafficType;

    // Severity of the traffic
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TrafficSeverity severity;

    // When the traffic was encountered
    @Column(nullable = false)
    private LocalDateTime occurredAt;

    // Duration of the traffic delay in minutes
    @Column
    private int delayMinutes;

    // How fast the bus was moving (km/h) at the time of traffic
    @Column
    private double speedAtEncounter;

    // Normal speed expected on this segment (km/h)
    @Column
    private double normalSpeed;

    // Distance from origin to traffic location (km)
    @Column
    private double distanceFromOriginKm;

    // Approximate location - latitude
    @Column
    private double latitude;

    // Approximate location - longitude
    @Column
    private double longitude;

    @Column
    private long trafficDurationMinutes;


    @OneToMany(mappedBy = "traffic", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private final Collection<TripTrafficRange> trafficRanges = new HashSet<>();


    public void addTrafficRange(TripTrafficRange trafficRange) {
        this.trafficRanges.add(trafficRange);
    }


}