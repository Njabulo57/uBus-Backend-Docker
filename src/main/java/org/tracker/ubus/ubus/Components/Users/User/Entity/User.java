package org.tracker.ubus.ubus.Components.Users.User.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.tracker.ubus.ubus.Components.Auth.Exception.Internal.UserNotStudentException;
import org.tracker.ubus.ubus.Components.Shared.Entities.TimeAuditableEntity;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users", indexes = {
        @Index(name = "idx_value_email", columnList= "email"),
        @Index(name = "idx_value_role", columnList = "role"),
        @Index(name = "idx_value_status", columnList = "status")
})
public class User extends TimeAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
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
        if (UserRole.STUDENT.equals(this.role))
            return this.email.substring(0, 9);
        return "";
    }


    public String getPhoneNumber() {
        if (phoneNumber == null || phoneNumber.isEmpty())
            return "";

        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");

        // Check if it's a South African number
        if (cleaned.startsWith("+27")) {
            // Format as +27 XX XXX XXXX
            String afterPlus = cleaned.substring(3);
            if (afterPlus.length() >= 9) {
                return String.format("+27 %s %s %s",
                        afterPlus.substring(0, 2),
                        afterPlus.substring(2, 5),
                        afterPlus.substring(5, 9));
            }
        }

        // Check for 27 at start (without plus)
        if (cleaned.startsWith("27") && cleaned.length() >= 11) {
            return String.format("+27 %s %s %s",
                    cleaned.substring(2, 4),
                    cleaned.substring(4, 7),
                    cleaned.substring(7, 11));
        }

        //format: XXX-XXX-XXXX
        if (cleaned.length() == 10) {
            return String.format("%s-%s-%s",
                    cleaned.substring(0, 3),
                    cleaned.substring(3, 6),
                    cleaned.substring(6, 10));
        }

        // For 9-digit numbers: XXX-XXX-XXX
        if (cleaned.length() == 9) {
            return String.format("%s-%s-%s",
                    cleaned.substring(0, 3),
                    cleaned.substring(3, 6),
                    cleaned.substring(6, 9));
        }
        //return as is
        return phoneNumber;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof User user))
            return false;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstname, lastname,
                email, password, role);
    }
}
