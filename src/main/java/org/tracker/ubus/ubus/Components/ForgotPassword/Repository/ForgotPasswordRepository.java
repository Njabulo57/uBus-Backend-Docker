package org.tracker.ubus.ubus.Components.ForgotPassword.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.ForgotPassword.Entity.ForgotPassword;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;

import java.util.UUID;


@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, UUID> {


    boolean existsByToken(String token);

    boolean existsByUserId(UUID userId);


    default ForgotPassword findByUserOrThrow(UUID userId) {
        return this.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Forgot Password Entry Doesn't Exist"));
    }
}
