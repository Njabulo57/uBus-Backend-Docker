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

    // Bus models (actual bus names like Volvo, Toyota, etc.)
    private static final String[] BUS_MODELS = {
            "Toyota Coaster", "Hino Rainbow", "Isuzu Journey",
            "Mercedes Benz Sprinter", "MAN Lion's City", "Scania Touring",
            "Volvo B7R", "DAF SB220", "Iveco Crossway", "Solaris Urbino"
    };

    // South African province license plate codes
    private static final String[] PROVINCE_CODES = {
            "CA", "CF", "CY", "CJ", "CL", "CN", "CP", "CR", "CT", "CV", "CW", "CX", // Western Cape
            "EC", "EC1", "EC2", "EC3", // Eastern Cape
            "FS", "FS1", "FS2", // Free State
            "GP", "GP1", "GP2", "GP3", // Gauteng
            "KZN", "KZ", "KA", "KB", "KC", "KD", "KE", "KF", "KG", "KH", "KJ", "KK", "KL", "KM", "KN", "KP", "KR", "KS", "KT", "KV", "KW", "KX", // KwaZulu-Natal
            "L", "LIM", // Limpopo
            "M", "MP", // Mpumalanga
            "NC", "NC1", "NC2", // Northern Cape
            "NW", "NW1", "NW2", // North West
            "N", "ND", "NE", "NF", "NG", "NH", "NJ", "NK", "NL", "NM", "NN", "NP", "NR", "NS", "NT", "NV", "NW", "NX" // Northern Province (old)
    };

    // Letters for number plate suffix
    private static final String[] LETTERS = {"A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "R", "S", "T", "V", "W", "X", "Y", "Z"};

    @Override
    public void run(String... args) throws Exception {
        if (busRepository.count() == 0) {
            log.info("🚀 =============================================");
            log.info("🚀 INSERTING BUSES IN PARALLEL");
            log.info("🚀 =============================================");
            long startTime = System.currentTimeMillis();

            int totalBuses = FIRST_PREFIXES.length * 12; // 4 × 12 buses
            log.info("📊 Target: {} buses", totalBuses);
            log.info("   📝 Format: {PREFIX}_{SUFFIX}_{NUMBER} (e.g., APK_DFC_001)");
            log.info("   🔤 Prefixes: APK, DFC, APB, SWC");
            log.info("   🔢 Numbers: 001 to 012 for each prefix");
            log.info("   🚌 Electric (ELECTRIC): Routes with DFC or APK suffix");
            log.info("   🚌 Combustion (COMBUSTION): Routes with SWC or APB suffix");
            log.info("   🏷️ Registration: South African format e.g., CA 123-456");

            try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                // Create buses for each prefix and number (1-12)
                for (String firstPrefix : FIRST_PREFIXES) {
                    for (int number = 1; number <= 12; number++) {

                        final int busNumber = number;

                        // Generate a suffix that's different from the prefix
                        String suffix = generateSuffix(firstPrefix);

                        // Format: PREFIX_SUFFIX_NUMBER (e.g., APK_DFC_001)
                        String formattedName = firstPrefix + "_" + suffix + "_" + String.format("%03d", number);

                        // Generate South African registration number
                        String registrationNumber = generateSouthAfricanRegistration();

                        // Determine bus type based on the suffix
                        // DFC and APK routes use Electric
                        // SWC and APB routes use Combustion
                        BusType busType;
                        if (suffix.equals("DFC") || suffix.equals("APK")) {
                            busType = BusType.ELECTRIC;  // Electric bus
                        } else {
                            busType = BusType.COMBUSTION; // Combustion bus
                        }

                        String finalFormattedName = formattedName;
                        BusType finalBusType = busType;
                        String finalRegistrationNumber = registrationNumber;
                        futures.add(CompletableFuture.runAsync(() ->
                                insertBus(finalFormattedName, finalRegistrationNumber, finalBusType, busNumber), executor));
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

    private void insertBus(String busName, String registrationNumber, BusType busType, int number) {
        transactionTemplate.executeWithoutResult(status -> {
            Bus bus = Bus.builder()
                    .registrationNumber(registrationNumber) // South African format
                    .name(busName)  // This will be like "APK_DFC_001"
                    .model(BUS_MODELS[faker.random().nextInt(BUS_MODELS.length)]) // Actual bus model like "Volvo B7R"
                    .capacity(30 + faker.random().nextInt(41)) // 30-70 seats
                    .type(busType) // This will be ELECTRIC or COMBUSTION
                    .operationalStatus(getRandomOperationalStatus())
                    .activityStatus(getRandomActivityStatus())
                    .isActive(faker.random().nextBoolean())
                    .build();

            busRepository.save(bus);
            int saved = savedCount.incrementAndGet();

            if (saved % 10 == 0) {
                log.info("📊 Progress: {}/{} buses saved", saved, 48);
            }
        });
    }

    /**
     * Generates a South African license plate number
     * Format examples:
     * - CA 123-456 (Western Cape - older format)
     * - GP 123-456 (Gauteng - newer format)
     * - KZN 123-456 (KwaZulu-Natal)
     * - EC 123-456 (Eastern Cape)
     */
    private String generateSouthAfricanRegistration() {
        // Pick a random province code
        String provinceCode = PROVINCE_CODES[secureRandom.nextInt(PROVINCE_CODES.length)];

        // Generate 3-digit number (100-999)
        int numberPart = 100 + secureRandom.nextInt(900);

        // Generate 3 random letters for the suffix
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            letters.append(LETTERS[secureRandom.nextInt(LETTERS.length)]);
        }

        // 50% chance to use the older format with hyphen, 50% chance to use newer format without hyphen
        if (secureRandom.nextBoolean()) {
            // Older format: GP 123-456
            return String.format("%s %03d-%03d", provinceCode, numberPart, 100 + secureRandom.nextInt(900));
        } else {
            // Newer format: GP 123456
            return String.format("%s %03d%03d", provinceCode, numberPart, 100 + secureRandom.nextInt(900));
        }
    }

    /**
     * Alternative method that generates a more traditional South African plate
     * with letters at the end: CA 123-456 ABC
     */
    private String generateTraditionalSouthAfricanRegistration() {
        String provinceCode = PROVINCE_CODES[secureRandom.nextInt(PROVINCE_CODES.length)];
        int numberPart = 100 + secureRandom.nextInt(900);

        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            letters.append(LETTERS[secureRandom.nextInt(LETTERS.length)]);
        }

        return String.format("%s %03d-%03d %s", provinceCode, numberPart, 100 + secureRandom.nextInt(900), letters.toString());
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
        String generatedSuffix = FIRST_PREFIXES[secureRandom.nextInt(FIRST_PREFIXES.length)];
        while (prefix.equals(generatedSuffix))
            generatedSuffix = FIRST_PREFIXES[secureRandom.nextInt(FIRST_PREFIXES.length)];
        return generatedSuffix;
    }

}