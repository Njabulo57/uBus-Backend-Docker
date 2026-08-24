package org.tracker.ubus.ubus.Components.Attendence.Entity;


import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;


@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkAttendance extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User driver;

    @Column(nullable = false)
    private LocalDateTime signedAt;

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof WorkAttendance that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(driver, that.driver);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}


