package org.tracker.ubus.ubus.Components.User.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.tracker.ubus.ubus.Components.Auth.Exception.Internal.UserNotStudentException;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.User.Enum.UserStatus;

import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_value_email", columnList= "email")
})
public class User extends TimeAuditableEntity {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, unique = true)
    private UUID id;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phoneNumber;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserRole role;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserStatus status;



    public String getStudentNumber()
            throws UserNotStudentException {
        if(UserRole.STUDENT.equals(this.role))
            return this.email.substring(0, 9);

        throw new UserNotStudentException("User is not a student: they have role: " + this.role);
    }


}
