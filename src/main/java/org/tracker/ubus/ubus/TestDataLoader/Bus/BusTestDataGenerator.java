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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

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

    // First part prefixes (only these)
    private static final String[] FIRST_PREFIXES = {"APK", "DFC", "APB", "SWC"};

    // Bus models
    private static final String[] BUS_MODELS = {
            "Toyota Coaster", "Hino Rainbow", "Isuzu Journey",
            "Mercedes Benz Sprinter", "MAN Lion's City", "Scania Touring",
            "Volvo B7R", "DAF SB220", "Iveco Crossway", "Solaris Urbino"
    };

    @Override
    public void run(String... args) throws Exception {
        if (busRepository.count() == 0) {
            log.info("🚀 =============================================");
            log.info("🚀 INSERTING BUSES IN PARALLEL");
            log.info("🚀 =============================================");
            long startTime = System.currentTimeMillis();

            int totalBuses = FIRST_PREFIXES.length * 12; // 4 × 12 buses
            log.info("📊 Target: {} buses", totalBuses);
            log.info("   📝 Format: {PREFIX}_{NUMBER} (e.g., APK_001, DFC_002)");
            log.info("   🔤 Prefixes: APK, DFC, APB, SWC");
            log.info("   🔢 Numbers: 001 to 010 for each prefix");

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                // Create buses for each prefix and number (1-12)
                for (String firstPrefix : FIRST_PREFIXES) {
                    for (int number = 1; number <= 12; number++) {

                        final int busNumber = number;

                        String suffix = generateSuffix(firstPrefix);

                        String formattedName = firstPrefix + "_" + suffix;

                        if(number >= 10)
                            formattedName += "_0" + number;
                        else
                            formattedName += "_00" + number;

                        String finalFormattedName = formattedName;
                        futures.add(CompletableFuture.runAsync(() ->
                                insertBus(finalFormattedName, busNumber), executor));
                    }
                }

                // Wait for all buses to be created
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

    private void insertBus(String prefix, int number) {
        String busName = String.format("%s_%03d", prefix, number);

        transactionTemplate.executeWithoutResult(status -> {
            Bus bus = Bus.builder()
                    .name(busName)
                    .model(BUS_MODELS[faker.random().nextInt(BUS_MODELS.length)])
                    .capacity(30 + faker.random().nextInt(41)) // 30-70 seats
                    .type(getRandomBusType())
                    .operationalStatus(getRandomOperationalStatus())
                    .activityStatus(getRandomActivityStatus())
                    .isActive(faker.random().nextBoolean())
                    .build();

            busRepository.save(bus);
            int saved = savedCount.incrementAndGet();

            if (saved % 10 == 0) {
                log.info("📊 Progress: {}/{} buses saved", saved, 40);
            }
        });
    }

    private BusType getRandomBusType() {
        BusType[] types = BusType.values();
        return types[faker.random().nextInt(types.length)];
    }

    private BusOperationalStatus getRandomOperationalStatus() {
        // Weighted towards OPERATIONAL (more likely)
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
        BusActivityStatus[] statuses = BusActivityStatus.values();
        return statuses[faker.random().nextInt(statuses.length)];
    }


    private String generateSuffix(String prefix) {

        String generatedSuffix =  FIRST_PREFIXES[secureRandom.nextInt(FIRST_PREFIXES.length)];
        while (prefix.equals(generatedSuffix))
            generatedSuffix = FIRST_PREFIXES[secureRandom.nextInt(FIRST_PREFIXES.length)];
        return generatedSuffix;
    }

}