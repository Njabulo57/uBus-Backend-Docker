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
                log.info("   • {} - {}", route.name(), route.getLabel());
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
                    log.info("   • {}: {} buses", route.name(), count);
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
                    log.info("   ✅ Bus {} assigned to route: {} ({})",
                            bus.getName(), route.name(), route.getLabel());
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

        // Assign routes based on bus prefix
        return switch (prefix) {
            case "APK" -> {
                // APK buses serve routes starting from Auckland Park Kingsway
                Route[] apkRoutes = {Route.APK_APB, Route.APK_SWC};
                // Even numbers get APK_APB, odd get APK_SWC
                yield isEvenRoute ? apkRoutes[0] : apkRoutes[1];
            }
            case "APB" -> {
                // APB buses serve routes starting from Auckland Park Bunting Road
                Route[] apbRoutes = {Route.APB_APK, Route.APB_DFC};
                // Even numbers get APB_APK, odd get APB_DFC
                yield isEvenRoute ? apbRoutes[0] : apbRoutes[1];
            }
            case "DFC" -> {
                // DFC buses serve routes starting from Doornfontein
                Route[] dfcRoutes = {Route.DFC_SWC, Route.DFC_APB};
                // Even numbers get DFC_SWC, odd get DFC_APB
                yield isEvenRoute ? dfcRoutes[0] : dfcRoutes[1];
            }
            case "SWC" -> {
                // SWC buses serve routes starting from Soweto
                Route[] swcRoutes = {Route.SWC_DFC, Route.SWC_APK};
                // Even numbers get SWC_DFC, odd get SWC_APK
                yield isEvenRoute ? swcRoutes[0] : swcRoutes[1];
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
}