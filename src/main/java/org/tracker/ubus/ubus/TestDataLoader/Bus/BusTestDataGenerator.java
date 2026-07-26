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

    private static final String[] PROVINCE_CODES = {
            "CA", "CF", "CY", "CJ", "CL", "CN", "CP", "CR", "CT", "CV", "CW", "CX",
            "EC", "EC1", "EC2", "EC3", "FS", "FS1", "FS2",
            "GP", "GP1", "GP2", "GP3",
            "KZN", "KZ", "KA", "KB", "KC", "KD", "KE", "KF", "KG", "KH", "KJ", "KK",
            "KL", "KM", "KN", "KP", "KR", "KS", "KT", "KV", "KW", "KX",
            "L", "LIM", "M", "MP",
            "NC", "NC1", "NC2", "NW", "NW1", "NW2",
            "N", "ND", "NE", "NF", "NG", "NH", "NJ", "NK", "NL", "NM", "NN", "NP",
            "NR", "NS", "NT", "NV", "NW", "NX"
    };

    @Override
    public void run(String... args) throws Exception {
        if (busRepository.count() == 0) {
            log.info("🚀 =============================================");
            log.info("🚀 CREATING BUSES WITH OPTIONAL ROUTES");
            log.info("🚀 =============================================");
            long startTime = System.currentTimeMillis();

            List<BusDefinition> busDefinitions = new ArrayList<>();

            // ===== ROUTE 1 BUSES: DFC 1-7 (7 buses with route) =====
            for (int i = 1; i <= 7; i++) {
                BusType busType = (i <= 4) ? BusType.ELECTRIC : BusType.COMBUSTION;
                busDefinitions.add(new BusDefinition("DFC " + i, Route.ROUTE_1, busType));
            }

            // ===== ROUTE 2 BUSES: SWC 1-7 (7 buses with route) =====
            for (int i = 1; i <= 7; i++) {
                BusType busType = (i <= 4) ? BusType.COMBUSTION : BusType.ELECTRIC;
                busDefinitions.add(new BusDefinition("SWC " + i, Route.ROUTE_2, busType));
            }

            // ===== ROUTE JBS BUSES: JBS 1-7 (7 buses with route) =====
            for (int i = 1; i <= 7; i++) {
                BusType busType = (i <= 3) ? BusType.COMBUSTION : BusType.ELECTRIC;
                busDefinitions.add(new BusDefinition("JBS " + i, Route.ROUTE_JBS, busType));
            }

            // ===== UNASSIGNED BUSES (route = null): 4 spare buses =====
            for (int i = 1; i <= 4; i++) {
                BusType busType = (i <= 2) ? BusType.ELECTRIC : BusType.COMBUSTION;
                busDefinitions.add(new BusDefinition("SPARE " + i, null, busType));
            }

            log.info("📊 Total buses: {}", busDefinitions.size());
            log.info("   🚌 DFC 1-7   → Route 1 (7 buses)");
            log.info("   🚌 SWC 1-7   → Route 2 (7 buses)");
            log.info("   🚌 JBS 1-7   → Route JBS (7 buses)");
            log.info("   🚌 SPARE 1-4 → No route assigned (4 buses)");

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
            log.info("   🚌 With route assigned: {}", busDefinitions.stream().filter(b -> b.primaryRoute != null).count());
            log.info("   🚌 Without route (SPARE): {}", busDefinitions.stream().filter(b -> b.primaryRoute == null).count());
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
                    .route(primaryRoute) // Can be null for spare buses
                    .operationalStatus(getRandomOperationalStatus())
                    .activityStatus(getRandomActivityStatus())
                    .isActive(faker.random().nextBoolean())
                    .build();

            busRepository.save(bus);
            int saved = savedCount.incrementAndGet();

            if (saved % 5 == 0) {
                log.info("📊 Progress: {}/{} buses saved", saved, 25);
            }
        });
    }

    private String generateSouthAfricanRegistration() {
        String provinceCode = PROVINCE_CODES[secureRandom.nextInt(PROVINCE_CODES.length)];
        int numberPart = 100 + secureRandom.nextInt(900);

        if (secureRandom.nextBoolean()) {
            return String.format("%s %03d-%03d", provinceCode, numberPart, 100 + secureRandom.nextInt(900));
        } else {
            return String.format("%s %03d%03d", provinceCode, numberPart, 100 + secureRandom.nextInt(900));
        }
    }

    private BusOperationalStatus getRandomOperationalStatus() {
        int random = faker.random().nextInt(100);
        if (random < 70) {
            return BusOperationalStatus.OPERATIONAL;
        } else if (random < 85) {
            return BusOperationalStatus.MAINTENANCE;
        } else {
            return BusOperationalStatus.OUT_OF_SERVICE;
        }
    }

    private BusActivityStatus getRandomActivityStatus() {
        BusActivityStatus[] statuses = { LOADING_PASSENGERS,
                ON_TRIP,         // En route with passengers
                STATIONERY,
                BREAK
        };
        // Driver break}
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