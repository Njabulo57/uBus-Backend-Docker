package org.tracker.ubus.ubus.TestDataLoader.Bus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.tracker.ubus.ubus.Components.Buses.Bus.Entity.Bus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusOperationalStatus;
import org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusType;
import org.tracker.ubus.ubus.Components.Buses.Bus.Repository.DatabaseAccessLayer.BusRepository;
import org.tracker.ubus.ubus.Components.Users.User.Enum.Route;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.tracker.ubus.ubus.Components.Buses.Bus.Enum.BusActivityStatus.*;

@Slf4j
@Order(2)
@Component
@RequiredArgsConstructor
public class BusTestDataGenerator implements CommandLineRunner {

    private final Faker faker;
    private final SecureRandom secureRandom;
    private final BusRepository busRepository;
    private final TransactionTemplate transactionTemplate;
    private final AtomicInteger savedCount = new AtomicInteger(0);

    private static final String[] BUS_MODELS = {
            "Toyota Coaster", "Hino Rainbow", "Isuzu Journey",
            "Mercedes Benz Sprinter", "MAN Lion's City", "Scania Touring",
            "Volvo B7R", "DAF SB220", "Iveco Crossway", "Solaris Urbino",
            "BYD K9", "Yutong E12", "Golden Dragon XML", "King Long XMQ",
            "New Flyer Xcelsior", "Proterra Catalyst", "Alexander Dennis Enviro400",
            "Wright Eclipse", "Optare Solo", "Van Hool A330"
    };

    // Common South African Province Codes
    private static final String[] PROVINCE_CODES = {
            "GP",   // Gauteng
            "KZN",  // KwaZulu-Natal
            "WC",   // Western Cape
            "EC",   // Eastern Cape
            "FS",   // Free State
            "MP",   // Mpumalanga
            "NW",   // North West
            "LIM",  // Limpopo
            "NC"    // Northern Cape
    };

    @Override
    public void run(String... args) throws Exception {
        if (busRepository.count() == 0) {
            log.info("🚀 =============================================");
            log.info("🚀 CREATING 20 BUSES WITH ROUTES");
            log.info("🚀 =============================================");
            long startTime = System.currentTimeMillis();

            List<BusDefinition> busDefinitions = new ArrayList<>();

            // ============================================
            // ROUTE 1: DFC ↔ APB ↔ APK (5 buses)
            // ============================================
            for (int i = 1; i <= 5; i++) {
                BusType type = (i % 2 == 0) ? BusType.ELECTRIC : BusType.COMBUSTION;
                busDefinitions.add(new BusDefinition(
                        "DFC-APK " + i,
                        Route.ROUTE_1,
                        type
                ));
            }

            // ============================================
            // ROUTE 2: SWC ↔ APK ↔ APB (5 buses)
            // ============================================
            for (int i = 1; i <= 5; i++) {
                BusType type = (i % 2 == 0) ? BusType.ELECTRIC : BusType.COMBUSTION;
                busDefinitions.add(new BusDefinition(
                        "SWC-APB " + i,
                        Route.ROUTE_2,
                        type
                ));
            }

            // ============================================
            // ROUTE 3: SWC ↔ DFC (5 buses)
            // ============================================
            for (int i = 1; i <= 5; i++) {
                BusType type = (i % 2 == 0) ? BusType.ELECTRIC : BusType.COMBUSTION;
                busDefinitions.add(new BusDefinition(
                        "SWC-DFC " + i,
                        Route.ROUTE_3,
                        type
                ));
            }

            // ============================================
            // ROUTE JBS: APK ↔ APB ↔ JBS (5 buses)
            // ============================================
            for (int i = 1; i <= 5; i++) {
                BusType type = (i % 2 == 0) ? BusType.ELECTRIC : BusType.COMBUSTION;
                busDefinitions.add(new BusDefinition(
                        "APK-JBS " + i,
                        Route.ROUTE_JBS,
                        type
                ));
            }

            log.info("📊 Total buses: {}", busDefinitions.size());
            log.info("   🚌 ROUTE_1 (DFC-APK): 5 buses");
            log.info("   🚌 ROUTE_2 (SWC-APB): 5 buses");
            log.info("   🚌 ROUTE_3 (SWC-DFC): 5 buses");
            log.info("   🚌 ROUTE_JBS (APK-JBS): 5 buses");

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                for (BusDefinition busDef : busDefinitions) {
                    String registrationNumber = generateSouthAfricanRegistration();
                    futures.add(CompletableFuture.runAsync(() ->
                            insertBus(busDef.name, registrationNumber, busDef.type, busDef.primaryRoute), executor));
                }

                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("🎉 =============================================");
            log.info("✅ COMPLETED! Saved {} buses in {} ms", savedCount.get(), elapsed);
            log.info("🎉 =============================================");
        } else {
            log.info("✅ Buses already exist - skipping data generation");
        }
    }

    private void insertBus(String busName, String registrationNumber, BusType busType, Route primaryRoute) {
        transactionTemplate.executeWithoutResult(status -> {
            Bus bus = Bus.builder()
                    .registrationNumber(registrationNumber)
                    .name(busName)
                    .model(BUS_MODELS[faker.random().nextInt(BUS_MODELS.length)])
                    .capacity(30 + faker.random().nextInt(41))
                    .type(busType)
                    .route(primaryRoute)
                    .operationalStatus(BusOperationalStatus.OPERATIONAL)
                    .activityStatus(getRandomActivityStatus())
                    .isActive(true)
                    .build();

            busRepository.save(bus);
            int saved = savedCount.incrementAndGet();

            if (saved % 5 == 0 || saved == 20) {
                log.info("📊 Progress: {}/20 buses saved", saved);
            }
        });
    }

    /**
     * Generate authentic South African registration plates
     * Format: ABC 123 GP, XYZ 456 KZN, DEF 789 WC
     * (3 letters, space, 3 digits, space, province code)
     */
    private String generateSouthAfricanRegistration() {
        // Generate 3 random letters (A-Z)
        char letter1 = (char) ('A' + secureRandom.nextInt(26));
        char letter2 = (char) ('A' + secureRandom.nextInt(26));
        char letter3 = (char) ('A' + secureRandom.nextInt(26));
        String letters = "" + letter1 + letter2 + letter3;

        // Generate 3 random digits (100-999)
        int digits = 100 + secureRandom.nextInt(900);

        // Random province code
        String provinceCode = PROVINCE_CODES[secureRandom.nextInt(PROVINCE_CODES.length)];

        return String.format("%s %03d %s", letters, digits, provinceCode);
    }

    private BusActivityStatus getRandomActivityStatus() {
        BusActivityStatus[] statuses = { LOADING_PASSENGERS, ON_TRIP, STATIONERY, BREAK };
        return statuses[faker.random().nextInt(statuses.length)];
    }

    private static class BusDefinition {
        final String name;
        final Route primaryRoute;
        final BusType type;

        BusDefinition(String name, Route primaryRoute, BusType type) {
            this.name = name;
            this.primaryRoute = primaryRoute;
            this.type = type;
        }
    }
}