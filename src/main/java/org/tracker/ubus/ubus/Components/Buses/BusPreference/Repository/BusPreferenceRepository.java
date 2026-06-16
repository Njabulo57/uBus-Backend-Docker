package org.tracker.ubus.ubus.Components.Buses.BusPreference.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Exceptions.BusPreferenceNotFoundException;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusPreferenceRepository extends JpaRepository<BusPreference, UUID> {

    @Query("""
        SELECT bp
        FROM BusPreference bp
        LEFT JOIN FETCH bp.user
        WHERE bp.user.id = :userId
    """)
    Optional<List<BusPreference>> findByBusUserId(@Param("userId") UUID userId);

    default List<BusPreference> findByUserOrThrow(UUID userId) throws BusPreferenceNotFoundException {
        return this.findByBusUserId(userId)
                .orElseThrow(() ->
                        new BusPreferenceNotFoundException(
                                String.format("No BusPreference found for User %s", userId)));
    }

    boolean existsByUserAndRoute(User user, Route route);

    int countByUser(User user);

    BusPreference findByUserAndRoute(User currentUser, Route oldRoute);

    List<BusPreference> findAllByUser(User user);
}
