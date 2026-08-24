package org.tracker.ubus.ubus.Components.Trips.TripUser.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Trips.Trip.Entity.Trip;
import org.tracker.ubus.ubus.Components.Trips.TripUser.Enum.TripUserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import java.util.UUID;

import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.STAFF;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.STUDENT;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_trip_user_user_trip",
                columnNames = {"user_id", "trip_id"}
        ),
        indexes = {
                @Index(name = "idx_trip_user_user", columnList = "user_id"),
                @Index(name = "idx_trip_user_trip", columnList = "trip_id"),
                @Index(name = "idx_trip_user_status", columnList = "status"),
                @Index(name = "idx_trip_user_user_trip", columnList = "user_id, trip_id"),
                @Index(name = "idx_trip_user_trip_status", columnList = "trip_id, status"),
                @Index(name = "idx_trip_user_user_status", columnList = "user_id, status"),
                @Index(name = "idx_trip_user_first_trip", columnList = "is_first_trip"),
                @Index(name = "idx_trip_user_trip_first_trip", columnList = "trip_id, is_first_trip"),
                @Index(name = "idx_trip_user_user_trip_status", columnList = "user_id, trip_id, status")
        }
)
public class TripUser extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @JoinColumn(nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @JoinColumn(nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Trip trip;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TripUserStatus status = TripUserStatus.IN_BUS;

    protected void validateUserRole() {
        var role = this.user.getRole();
        if(!(role == STAFF) && !(role == STUDENT))
            throw new IllegalStateException("User is not a " + this.user.getRole().name() );
    }
    @JoinColumn(nullable = false, updatable = false)
    private boolean isFirstTrip = true;

}
