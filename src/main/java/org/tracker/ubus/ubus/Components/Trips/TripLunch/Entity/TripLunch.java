package org.tracker.ubus.ubus.Components.Trips.TripLunch.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripLunch extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    private LocalTime time;



    @JoinColumn(nullable = false)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Bus bus;
}
