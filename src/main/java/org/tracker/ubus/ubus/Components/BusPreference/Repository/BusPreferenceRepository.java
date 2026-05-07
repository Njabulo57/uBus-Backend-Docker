package org.tracker.ubus.ubus.Components.BusPreference.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.BusPreference.Exceptions.BusPreferenceNotFoundException;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusPreferenceRepository extends JpaRepository<BusPreference, UUID> {

    @Query("""
        SELECT bp
        FROM BusPreference bp
        LEFT JOIN FETCH bp.busUser
        WHERE bp.busUser.id = :userId
    """)
    Optional<BusPreference> findByUser(@Param("userId") UUID userId);


    default BusPreference findByUserOrThrow(UUID userId) throws BusPreferenceNotFoundException {
        return this.findByUser(userId)
                .orElseThrow(() ->
                        new BusPreferenceNotFoundException(String.format("No BusPreference found for User %s", userId)));
    }
}
