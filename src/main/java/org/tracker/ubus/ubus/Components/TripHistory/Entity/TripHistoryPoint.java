package org.tracker.ubus.ubus.Components.TripHistory.Entity;

import lombok.*;
import jakarta.persistence.*;
import org.tracker.ubus.ubus.Components.Trip.Entity.Trip;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripHistoryPoint {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @Column(nullable = false, updatable = false)
    private double latitude;

    @Column(nullable = false, updatable = false)
    private double longitude;

    @Column(nullable = false, updatable = false)
    private double speed;

    @JoinColumn(nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Trip trip;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timePosted;

}
