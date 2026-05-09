package org.tracker.ubus.ubus.Components.OneTimePassword.Reposirtory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.OneTimePassword.Entity.OneTimePassword;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordNotFoundException;
import org.tracker.ubus.ubus.Components.User.Entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OneTimePasswordRepository extends JpaRepository<OneTimePassword, UUID> {

    @Query("""
        SELECT CASE WHEN COUNT(otp) > 0 THEN true ELSE false END
        FROM OneTimePassword otp
        WHERE otp.user.id = :userId
    """)
    boolean existsByUser(@Param("userId") UUID userId);

    boolean existsByOtp(String otp);


    @Query("""
        SELECT otp FROM OneTimePassword otp
        LEFT JOIN FETCH otp.user
        WHERE otp.user.id = :userId
    """)
    Optional<OneTimePassword> findByUser(UUID userId);


    @Query("""
        SELECT otp FROM OneTimePassword otp
        LEFT JOIN otp.user
        WHERE otp.otp = :otp
    """)
    Optional<OneTimePassword> findByOtp(@Param("otp") String otp);


    @Query("""
        SELECT otp FROM OneTimePassword otp
        LEFT JOIN otp.user
    """)
    List<OneTimePassword> findAllOTPs();

    default OneTimePassword findByUserOrThrow(UUID userId) {
        return this.findByUser(userId)
                .orElseThrow(() -> new OneTimePasswordNotFoundException("OTP Doesn't Exist"));
    }

}