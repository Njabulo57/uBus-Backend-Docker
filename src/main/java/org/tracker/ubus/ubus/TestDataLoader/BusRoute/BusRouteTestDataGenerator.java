package org.tracker.ubus.ubus.TestDataLoader.BusRoute;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Buses.BusRoute.Entity.BusRoute;
import org.tracker.ubus.ubus.Components.Buses.BusRoute.Repository.BusRouteRepository;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Order(4)
@Component
@RequiredArgsConstructor
public class BusRouteTestDataGenerator implements CommandLineRunner {

    private final BusRepository busRepository;
    private final BusRouteRepository busRouteRepository;
    private final TransactionTemplate transactionTemplate;
    private final SecureRandom secureRandom;
    private final AtomicInteger savedCount = new AtomicInteger(0);

    // Route assignment mapping based on bus prefix
    private static final Route[] APK_ROUTES = {Route.APK_TO_APB_DFC, Route.APK_TO_APB_JBS};
    private static final Route[] APB_ROUTES = {Route.APB_TO_APK_SWC, Route.APK_TO_APB_JBS};
    private static final Route[] DFC_ROUTES = {Route.DFC_TO_APB_APK, Route.DFC_TO_SWC};
    private static final Route[] SWC_ROUTES = {Route.SWC_TO_APK_APB, Route.SWC_TO_DFC};

    @Override
    public void run(String... args) throws Exception {
        long busCount = busRepository.count();

        if (busCount > 0 && busRouteRepository.count() == 0) {
            log.info("🚌 =============================================");
            log.info("🚌 CREATING BUS ROUTES FOR ALL BUSES");
            log.info("🚌 =============================================");
            long startTime = System.currentTimeMillis();

            List<Bus> allBuses = busRepository.findAll();
            log.info("📊 Found {} buses to assign routes", allBuses.size());
            log.info("🗺️  Available routes:");
            for (Route route : Route.values()) {
                log.info("   • {} - {} ({})", route.name(), route.getLabel(), route.getRouteNumber());
            }

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                for (Bus bus : allBuses) {
                    futures.add(CompletableFuture.runAsync(() ->
                            createBusRoute(bus), executor));
                }

                // Wait for all routes to be created
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("🎉 =============================================");
            log.info("✅ COMPLETED! Created {} bus routes in {} ms", savedCount.get(), elapsed);

            // Log summary by route type
            log.info("📊 Route distribution summary:");
            for (Route route : Route.values()) {
                long count = busRouteRepository.findByRoute(route).size();
                if (count > 0) {
                    log.info("   • {}: {} buses", route.getRouteNumber(), count);
                }
            }
            log.info("🎉 =============================================");
        } else if (busRouteRepository.count() > 0) {
            log.info("✅ Bus routes already exist - skipping data generation");
        } else {
            log.warn("⚠️ No buses found - please run BusTestDataGenerator first");
        }
    }

    private void createBusRoute(Bus bus) {
        transactionTemplate.executeWithoutResult(status -> {
            try {
                // Assign route based on bus prefix
                Route route = assignRouteToBus(bus);

                BusRoute busRoute = BusRoute.builder()
                        .bus(bus)
                        .route(route)
                        .build();

                busRouteRepository.save(busRoute);
                int saved = savedCount.incrementAndGet();

                // Log progress with meaningful route info
                if (saved % 5 == 0 || saved <= 10) {
                    log.info("📊 Progress: {}/{} routes created", saved, busRepository.count());
                }

                if (saved <= 10) { // Log first 10 assignments for visibility
                    log.info("   ✅ Bus {} assigned to route: {} ({} - {})",
                            bus.getName(), route.name(), route.getRouteNumber(), route.getLabel());
                }
            } catch (Exception e) {
                log.error("❌ Failed to create route for bus {}: {}", bus.getName(), e.getMessage());
                status.setRollbackOnly();
            }
        });
    }

    private Route assignRouteToBus(Bus bus) {
        String busName = bus.getName();

        // Extract prefix from bus name (e.g., "APK_001" -> "APK")
        String prefix = busName.split("_")[0];

        // Extract the bus number to create some variation (odd/even routes)
        int busNumber = extractBusNumber(busName);
        boolean isEvenRoute = busNumber % 2 == 0;

        // Assign routes based on bus prefix and number
        return switch (prefix) {
            case "APK" -> {
                // APK buses: even -> APK_TO_APB_DFC, odd -> APK_TO_APB_JBS
                yield isEvenRoute ? APK_ROUTES[0] : APK_ROUTES[1];
            }
            case "APB" -> {
                // APB buses: even -> APB_TO_APK_SWC, odd -> APK_TO_APB_JBS
                yield isEvenRoute ? APB_ROUTES[0] : APB_ROUTES[1];
            }
            case "DFC" -> {
                // DFC buses: even -> DFC_TO_APB_APK, odd -> DFC_TO_SWC
                yield isEvenRoute ? DFC_ROUTES[0] : DFC_ROUTES[1];
            }
            case "SWC" -> {
                // SWC buses: even -> SWC_TO_APK_APB, odd -> SWC_TO_DFC
                yield isEvenRoute ? SWC_ROUTES[0] : SWC_ROUTES[1];
            }
            default -> {
                // Fallback - random route for any unexpected prefixes
                Route[] allRoutes = Route.values();
                yield allRoutes[secureRandom.nextInt(allRoutes.length)];
            }
        };
    }

    private int extractBusNumber(String busName) {
        try {
            // Format: APK_001, DFC_012, etc.
            String[] parts = busName.split("_");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException e) {
            log.debug("Could not parse bus number from: {}", busName);
        }
        // Return a random number if parsing fails
        return secureRandom.nextInt(100);
    }

    /**
     * Get the appropriate route for a bus based on its prefix and route number
     * Useful for manual assignment
     */
    public Route getRouteForBus(String prefix, int busNumber) {
        boolean isEvenRoute = busNumber % 2 == 0;

        return switch (prefix) {
            case "APK" -> isEvenRoute ? Route.APK_TO_APB_DFC : Route.APK_TO_APB_JBS;
            case "APB" -> isEvenRoute ? Route.APB_TO_APK_SWC : Route.APK_TO_APB_JBS;
            case "DFC" -> isEvenRoute ? Route.DFC_TO_APB_APK : Route.DFC_TO_SWC;
            case "SWC" -> isEvenRoute ? Route.SWC_TO_APK_APB : Route.SWC_TO_DFC;
            default -> {
                Route[] allRoutes = Route.values();
                yield allRoutes[secureRandom.nextInt(allRoutes.length)];
            }
        };
    }
}