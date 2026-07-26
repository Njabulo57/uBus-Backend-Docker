package org.tracker.ubus.ubus.TestDataLoader.BusPreference;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Entity.BusPreference;
import org.tracker.ubus.ubus.Components.Buses.BusPreference.Repository.BusPreferenceRepository;
import org.tracker.ubus.ubus.Components.Buses.BusUserPreferenceDentination.Entity.BusUserPreferenceDestination;
import org.tracker.ubus.ubus.Components.Trips.Trip.Enum.Destination;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.util.*;

@Slf4j
@Order(7)
@Component
@RequiredArgsConstructor
public class BusPreferenceDattaLoader implements CommandLineRunner {

    private final Random random = new Random();
    private final BusPreferenceRepository busPreferenceRepository;
    private final UserRepository userRepository;

    // All possible destination pairs from routes
    private final List<Map<Destination, Destination>> possiblePreferencePairs = List.of(
            Map.of(Destination.DFC, Destination.APK),
            Map.of(Destination.APK, Destination.DFC),
            Map.of(Destination.SWC, Destination.APB),
            Map.of(Destination.APB, Destination.SWC),
            Map.of(Destination.SWC, Destination.DFC),
            Map.of(Destination.DFC, Destination.SWC),
            Map.of(Destination.APK, Destination.JBS),
            Map.of(Destination.JBS, Destination.APK),
            Map.of(Destination.DFC, Destination.APB),
            Map.of(Destination.APB, Destination.DFC),
            Map.of(Destination.SWC, Destination.APK),
            Map.of(Destination.APK, Destination.SWC)
    );

    @Override
    public void run(String... args) throws Exception {
        //this.makePreferences();
    }

    /**
     * Returns a random number of preferences (1-4) with more weight on complex preferences
     */
    private int getRandomPreferenceCount() {
        // Weighted random: more likely to getFromTripSimulationCache 3-4 preferences
        int[] weights = {10, 20, 35, 35}; // 10% for 1, 20% for 2, 35% for 3, 35% for 4
        int totalWeight = 0;
        for (int w : weights) {
            totalWeight += w;
        }

        int randomValue = random.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (randomValue < cumulative) {
                return i + 1; // returns 1-4
            }
        }
        return 3; // default
    }

    /**
     * Gets a list of unique destination pairs
     */
    private List<Map<Destination, Destination>> getDestinationPairs(int numPreferences) {
        List<Map<Destination, Destination>> selectedPairs = new ArrayList<>();
        Set<String> usedCombinations = new HashSet<>();

        // Shuffle the possible pairs to getFromTripSimulationCache random selection
        List<Map<Destination, Destination>> shuffledPairs = new ArrayList<>(possiblePreferencePairs);
        Collections.shuffle(shuffledPairs, random);

        for (int i = 0; i < numPreferences && i < shuffledPairs.size(); i++) {
            var pair = shuffledPairs.get(i);
            Destination from = pair.keySet().iterator().next();
            Destination to = pair.values().iterator().next();
            String key = from.name() + "->" + to.name();

            // Make sure we don't add duplicate combinations
            if (!usedCombinations.contains(key)) {
                selectedPairs.add(pair);
                usedCombinations.add(key);
            }
        }

        return selectedPairs;
    }

    /**
     * Finds a route that contains all the destinations in the pairs
     */
    private Route findRouteForDestinations(List<Map<Destination, Destination>> destinationPairs) {
        // Collect all unique destinations from the pairs
        Set<Destination> allDestinations = new HashSet<>();
        for (var pair : destinationPairs) {
            allDestinations.add(pair.keySet().iterator().next());
            allDestinations.add(pair.values().iterator().next());
        }

        // Check each route to see if it contains all destinations
        for (Route route : Route.values()) {
            if (new HashSet<>(route.getDestinations()).containsAll(allDestinations)) {
                return route;
            }
        }

        return null;
    }

    private void makePreferences() {
        var listRoles = List.of(UserRole.STAFF, UserRole.STUDENT);
        var staffAndStudents = userRepository.findByRoleInAndStatus(listRoles, UserStatus.ACTIVE);
        var existingBusPreferences = busPreferenceRepository.findAll();

        // Get users who already have preferences
        var usersWithPreferences = existingBusPreferences.stream()
                .map(BusPreference::getUser)
                .toList();

        int preferencesCreated = 0;

        for (var user : staffAndStudents) {
            // Skip if user already has preferences
            if (usersWithPreferences.contains(user)) {
                continue;
            }

            // Determine how many preferences this user gets (1-4)
            int numPreferences = getRandomPreferenceCount();

            // Get random destination pairs
            var destinationPairs = getDestinationPairs(numPreferences);

            // Get a route that contains all these destinations
            Route route = findRouteForDestinations(destinationPairs);

            if (route == null) {
                log.warn("No route found for destinations: {}", destinationPairs);
                continue;
            }

            // Create the BusPreference
            var busPreference = BusPreference.builder()
                    .user(user)
                    .route(route)
                    .build();

            // Add the destination pairs as BusUserPreferenceDestination
            for (var pair : destinationPairs) {
                Destination from = pair.keySet().iterator().next();
                Destination to = pair.values().iterator().next();

                var prefDest = BusUserPreferenceDestination.builder()
                        .fromDestination(from)
                        .toDestination(to)
                        .busPreference(busPreference)
                        .build();

                busPreference.addBusUserPrefDestination(prefDest);
            }

            busPreferenceRepository.save(busPreference);
            preferencesCreated++;

            log.debug("Created BusPreference with {} destinations for user: {}",
                    numPreferences, user.getEmail());
        }

        log.info("Created {} new bus preferences", preferencesCreated);

    }
 }