package org.tracker.ubus.ubus.Components.Buses.BusUserPreferenceDentination.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"bus_preference_id", "from_destination", "to_destination"})
})
public class BusUserPreferenceDestination  extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Destination fromDestination;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Destination toDestination;


    @JoinColumn( nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private BusPreference busPreference;



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BusUserPreferenceDestination that = (BusUserPreferenceDestination) o;
        return Objects.equals(id, that.id) && fromDestination == that.fromDestination && toDestination == that.toDestination && Objects.equals(busPreference, that.busPreference);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fromDestination, toDestination, busPreference);
    }
}
