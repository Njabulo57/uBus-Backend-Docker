package org.tracker.ubus.ubus.Components.OneTimePassword.Reposirtory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.OneTimePassword.Entity.OneTimePassword;
import org.tracker.ubus.ubus.Components.OneTimePassword.Exceptions.OneTimePasswordNotFoundException;
import org.tracker.ubus.ubus.Components.User.Entity.User;

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



    @Query("""
        SELECT otp FROM OneTimePassword otp
        LEFT JOIN FETCH otp.user
        WHERE otp.user.email = :email
    """)
    Optional<OneTimePassword> findByUser(@Param("email") String email);


    @Query("""
        SELECT otp FROM OneTimePassword otp
        LEFT JOIN FETCH otp.user
        WHERE otp.user = :user
    """)
    Optional<OneTimePassword> findByUser(@Param("user") User user);

    @Query("""
        SELECT otp FROM OneTimePassword otp
        LEFT JOIN FETCH otp.user
        WHERE otp.user = :user AND otp.expiresAt > CURRENT_TIMESTAMP
    """)
    Optional<OneTimePassword> findByUserAndNotExpired(@Param("user") User user);

    @Query("""
        SELECT CASE WHEN COUNT(otp) > 0 THEN true ELSE false END
        FROM OneTimePassword otp
        WHERE otp.user.id = :userId AND otp.expiresAt > CURRENT_TIMESTAMP
    """)
    boolean existsValidByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT otp FROM OneTimePassword otp
        LEFT JOIN FETCH otp.user
        WHERE otp.user.id = :userId AND otp.expiresAt > CURRENT_TIMESTAMP
    """)
    Optional<OneTimePassword> findByUserAndNotExpired(@Param("id") UUID userId);


    @Query("""
        SELECT otp FROM OneTimePassword otp
        LEFT JOIN FETCH otp.user
        WHERE otp.user.id = :userId
    """)
    Optional<OneTimePassword> findByUser(UUID userId);


    @Query("""
        SELECT otp FROM OneTimePassword otp
        LEFT JOIN otp.user
        WHERE otp.user.email = :email
    """)
    Optional<OneTimePassword> findByUserEmail(@Param("email") String email);


    @Query("""
        SELECT otp FROM OneTimePassword otp
        LEFT JOIN otp.user
        WHERE otp.otp = :otp
    """)
    Optional<OneTimePassword> findByOtp(@Param("otp") String otp);


    default OneTimePassword findByOtpOrThrow(String otp) {
        return this.findByOtp(otp)
                .orElseThrow(() ->
                        new OneTimePasswordNotFoundException("One Time Password Not Found for Provided Otp"));
    }

    default OneTimePassword findByUserAndNotExpiredOrThrow(UUID userId) {
        return findByUserAndNotExpired(userId)
                .orElseThrow(() -> new OneTimePasswordNotFoundException("OTP Doesn't Exist"));
    }

    default boolean existsAndExpired(UUID userId) throws OneTimePasswordNotFoundException {

        //if the opt doesnt exist we throw an exception
        var otpEntry = this.findByUser(userId)
                .orElseThrow(() -> new OneTimePasswordNotFoundException("OTP Doesnt Exist"));

        if (otpEntry.isExpired())
            return false;
        return true;
    }

    default OneTimePassword findByUserOrThrow(User user) {
        return this.findByUser(user)
                .orElseThrow(() -> new OneTimePasswordNotFoundException("OTP Doesn't Exist"));
    }

    default OneTimePassword findByUserOrThrow(String email)
            throws OneTimePasswordNotFoundException {
        return this.findByUser(email)
                .orElseThrow(() -> new OneTimePasswordNotFoundException("OTP Doesn't Exist"));

    }

    default OneTimePassword findByUserOrThrow(UUID userId) {
        return this.findByUser(userId)
                .orElseThrow(() -> new OneTimePasswordNotFoundException("OTP Doesn't Exist"));
    }

}