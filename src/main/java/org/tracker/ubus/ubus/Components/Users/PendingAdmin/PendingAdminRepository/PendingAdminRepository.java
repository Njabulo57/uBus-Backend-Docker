package org.tracker.ubus.ubus.Components.Users.PendingAdmin.PendingAdminRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.Entity.PendingAdmin;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PendingAdminRepository extends JpaRepository<PendingAdmin, UUID> {

    /**
     * Checks if an admin user with the specified email exists in the system.
     *
     * @param email the email address of the admin to check for existence
     * @return true if an admin with the specified email exists, false otherwise
     */
    boolean existsByEmail(String email);


    Optional<PendingAdmin> findByEmail(String email);

    @Query("""
        SELECT pa FROM PendingAdmin pa
        LEFT JOIN FETCH pa.createdBy
        WHERE pa.email = :email
        AND pa.createdBy = :createdBy
    """)
    Optional<PendingAdmin> findByEmailAndCreatedBy(@Param("email") String email, @Param("createdBy") User createdBy);


    default PendingAdmin findByEmailOrThrow(String email) {
        return this.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Pending Admin not found"));
    }



    default PendingAdmin findByEmailAndCreatorOrThrow(String email, User creator) {
        return this.findByEmailAndCreatedBy(email, creator)
                .orElseThrow(() -> new IllegalArgumentException("Pending Admin not found"));

    }

}
