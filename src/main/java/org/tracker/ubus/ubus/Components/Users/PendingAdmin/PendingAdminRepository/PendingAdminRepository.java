package org.tracker.ubus.ubus.Components.Users.PendingAdmin.PendingAdminRepository;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Users.PendingAdmin.Entity.PendingAdmin;


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


    default PendingAdmin findByEmailOrThrow(String email) {
        return this.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Pending Admin not found"));
    }



}
