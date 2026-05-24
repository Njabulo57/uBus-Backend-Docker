package org.tracker.ubus.ubus.TestDataLoader.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.User.Repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.tracker.ubus.ubus.Components.User.Enum.UserRole.*;
import static org.tracker.ubus.ubus.Components.User.Enum.UserStatus.*;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class UserTestDataGenerator implements CommandLineRunner {

    private final Faker faker;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("PARALLEL GENERATION + BATCH DATABASE SAVE");
            log.info("------------------------------------------------");
            long startTime = System.currentTimeMillis();

            List<User> allUsers = generateUsersInParallelWithDetailedLogs();

            int totalSaved = saveInBatches(allUsers);

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("------------------------------------------------");
            log.info("COMPLETED! Saved {} users in {} ms ({} seconds)",
                    totalSaved, elapsed, String.format("%.2f", elapsed / 1000.0));
            log.info("------------------------------------------------");

        } else {
            log.info("Users already exist, skipping data generation");
        }
    }

    private List<User> generateUsersInParallelWithDetailedLogs() throws InterruptedException {
        long genStart = System.currentTimeMillis();

        int totalUsers = 500 + 30 + 400 + 25;

        List<User> studentList = new CopyOnWriteArrayList<>();
        List<User> adminList = new CopyOnWriteArrayList<>();
        List<User> staffList = new CopyOnWriteArrayList<>();
        List<User> driverList = new CopyOnWriteArrayList<>();

        log.info("Generating {} users in PARALLEL using virtual threads...", totalUsers);
        log.info("");

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            // STUDENTS: 10 batches of 50
            log.info("------------------------------------------------");
            log.info("STUDENTS - 10 batches of 50");
            log.info("------------------------------------------------");

            CountDownLatch studentLatch = new CountDownLatch(10);
            AtomicInteger studentCount = new AtomicInteger(0);
            int studentBatchSize = 50;

            for (int batch = 0; batch < 10; batch++) {
                final int batchNumber = batch;
                final int startIndex = batch * studentBatchSize + 1;
                final int endIndex = (batch + 1) * studentBatchSize;

                executor.submit(() -> {
                    try {
                        long threadStart = System.nanoTime();
                        List<User> studentBatch = new ArrayList<>();

                        for (int i = startIndex; i <= endIndex; i++) {
                            studentBatch.add(createStudent(i));
                        }

                        studentList.addAll(studentBatch);

                        long threadTime = (System.nanoTime() - threadStart) / 1_000_000;
                        int completed = studentCount.addAndGet(studentBatchSize);
                        int percent = (completed * 100) / 500;

                        String bar = createProgressBar(percent);

                        log.info("  Batch {}/10 | {} | {}ms | {}/500 ({}%)",
                                batchNumber + 1,
                                bar,
                                threadTime,
                                completed,
                                percent
                        );
                    } finally {
                        studentLatch.countDown();
                    }
                });
            }

            // ADMINS: 30 individual threads
            log.info("");
            log.info("------------------------------------------------");
            log.info("ADMINS - 30 individual");
            log.info("------------------------------------------------");

            CountDownLatch adminLatch = new CountDownLatch(30);
            AtomicInteger adminCount = new AtomicInteger(0);

            for (int i = 1; i <= 30; i++) {
                final int adminNumber = i;
                executor.submit(() -> {
                    try {
                        long threadStart = System.nanoTime();
                        User user = createAdmin(adminNumber);
                        adminList.add(user);
                        long threadTime = (System.nanoTime() - threadStart) / 1_000_000;

                        int completed = adminCount.incrementAndGet();
                        int percent = (completed * 100) / 30;

                        String bar = createProgressBar(percent);

                        log.info("  Admin #{} | {} | {}ms | {}/30 ({}%)",
                                adminNumber,
                                bar,
                                threadTime,
                                completed,
                                percent
                        );
                    } finally {
                        adminLatch.countDown();
                    }
                });
            }

            // STAFF: 8 batches of 50
            log.info("");
            log.info("------------------------------------------------");
            log.info("STAFF - 8 batches of 50");
            log.info("------------------------------------------------");

            CountDownLatch staffLatch = new CountDownLatch(8);
            AtomicInteger staffCount = new AtomicInteger(0);
            int staffBatchSize = 50;

            for (int batch = 0; batch < 8; batch++) {
                final int batchNumber = batch;
                final int startIndex = batch * staffBatchSize + 1;
                final int endIndex = (batch + 1) * staffBatchSize;

                executor.submit(() -> {
                    try {
                        long threadStart = System.nanoTime();
                        List<User> staffBatch = new ArrayList<>();

                        for (int i = startIndex; i <= endIndex; i++) {
                            staffBatch.add(createStaff(i));
                        }

                        staffList.addAll(staffBatch);

                        long threadTime = (System.nanoTime() - threadStart) / 1_000_000;
                        int completed = staffCount.addAndGet(staffBatchSize);
                        int percent = (completed * 100) / 400;

                        String bar = createProgressBar(percent);

                        log.info("  Batch {}/8 | {} | {}ms | {}/400 ({}%)",
                                batchNumber + 1,
                                bar,
                                threadTime,
                                completed,
                                percent
                        );
                    } finally {
                        staffLatch.countDown();
                    }
                });
            }

            // DRIVERS: 25 individual threads
            log.info("");
            log.info("------------------------------------------------");
            log.info("DRIVERS - 25 individual");
            log.info("------------------------------------------------");

            CountDownLatch driverLatch = new CountDownLatch(25);
            AtomicInteger driverCount = new AtomicInteger(0);

            for (int i = 1; i <= 25; i++) {
                final int driverNumber = i;
                executor.submit(() -> {
                    try {
                        long threadStart = System.nanoTime();
                        User user = createDriver(driverNumber);
                        driverList.add(user);
                        long threadTime = (System.nanoTime() - threadStart) / 1_000_000;

                        int completed = driverCount.incrementAndGet();
                        int percent = (completed * 100) / 25;

                        String bar = createProgressBar(percent);

                        log.info("  Driver #{} | {} | {}ms | {}/25 ({}%)",
                                driverNumber,
                                bar,
                                threadTime,
                                completed,
                                percent
                        );
                    } finally {
                        driverLatch.countDown();
                    }
                });
            }

            log.info("");
            log.info("Waiting for all {} generation threads to complete...", totalUsers);
            log.info("");

            // Wait for ALL generation to complete with progress bars
            int lastPercent = -1;
            while (studentLatch.getCount() > 0 || adminLatch.getCount() > 0 ||
                    staffLatch.getCount() > 0 || driverLatch.getCount() > 0) {

                long remaining = studentLatch.getCount() + adminLatch.getCount() +
                        staffLatch.getCount() + driverLatch.getCount();
                long completed = totalUsers - remaining;
                int percent = (int)((completed * 100) / totalUsers);

                if (percent != lastPercent) {
                    String bar = createProgressBar(percent);
                    log.info("  OVERALL PROGRESS | {} | {}% | {}/{}", bar, percent, completed, totalUsers);
                    lastPercent = percent;
                }

                Thread.sleep(100);
            }

            String finalBar = createProgressBar(100);
            log.info("  OVERALL PROGRESS | {} | 100% | {}/{}", finalBar, totalUsers, totalUsers);

            studentLatch.await();
            adminLatch.await();
            staffLatch.await();
            driverLatch.await();
        }

        List<User> allUsers = new ArrayList<>();
        allUsers.addAll(studentList);
        allUsers.addAll(adminList);
        allUsers.addAll(staffList);
        allUsers.addAll(driverList);

        long generationTime = System.currentTimeMillis() - genStart;
        log.info("");
        log.info("------------------------------------------------");
        log.info("Parallel generation complete in {} ms", generationTime);
        log.info("Generated {} users total", allUsers.size());
        log.info("------------------------------------------------");
        log.info("");

        return allUsers;
    }

    private String createProgressBar(int percent) {
        int barLength = 30;
        int filledLength = (barLength * percent) / 100;

        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            if (i < filledLength) {
                bar.append("=");
            } else {
                bar.append(" ");
            }
        }
        bar.append("]");

        return bar.toString();
    }

    private int saveInBatches(List<User> users) {
        int batchSize = 500;
        int totalBatches = (int) Math.ceil((double) users.size() / batchSize);
        int totalSaved = 0;

        log.info("SAVING TO DATABASE (Batch Mode)");
        log.info("------------------------------------------------");
        log.info("Total users: {} | Batch size: {} | Total batches: {}",
                users.size(), batchSize, totalBatches);
        log.info("------------------------------------------------");
        log.info("");

        long saveStart = System.currentTimeMillis();

        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            int start = batchNum * batchSize;
            int end = Math.min(start + batchSize, users.size());
            List<User> batch = users.subList(start, end);

            long batchStart = System.currentTimeMillis();

            userRepository.saveAll(batch);
            userRepository.flush();

            long batchTime = System.currentTimeMillis() - batchStart;
            totalSaved += batch.size();

            int overallPercent = (totalSaved * 100) / users.size();

            String bar = createProgressBar(overallPercent);

            log.info("  Batch {}/{} | {} | {}ms | {}/{} ({}%)",
                    batchNum + 1,
                    totalBatches,
                    bar,
                    batchTime,
                    totalSaved,
                    users.size(),
                    overallPercent
            );
        }

        long saveTime = System.currentTimeMillis() - saveStart;
        log.info("");
        log.info("Database save complete in {} ms", saveTime);
        log.info("");

        return totalSaved;
    }

    private User createStudent(int studentNumber) {
        String studentNum = "2024" + String.format("%05d", studentNumber);

        return User.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(studentNum + "@student.ubus.ac.za")
                .password(passwordEncoder.encode("student@123"))
                .phoneNumber(generateSouthAfricanPhoneNumber())
                .role(STUDENT)
                .status(getRandomStatus(STUDENT))
                .build();
    }

    private User createAdmin(int index) {
        return User.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(String.format("admin%03d@admin.ubus.za", index))
                .password(passwordEncoder.encode("admin@123"))
                .phoneNumber(generateSouthAfricanPhoneNumber())
                .role(ADMIN)
                .status(getRandomStatus(ADMIN))
                .build();
    }

    private User createStaff(int index) {
        return User.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(String.format("staff%03d@ubus.ac.za", index))
                .password(passwordEncoder.encode("staff@123"))
                .phoneNumber(generateSouthAfricanPhoneNumber())
                .role(STAFF)
                .status(getRandomStatus(STAFF))
                .build();
    }

    private User createDriver(int index) {
        return User.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .email(String.format("driver%03d@ubus.co.za", index))
                .password(passwordEncoder.encode("driver@2025"))
                .phoneNumber(generateSouthAfricanPhoneNumber())
                .role(DRIVER)
                .status(getRandomStatus(DRIVER))
                .build();
    }

    private UserStatus getRandomStatus(UserRole role) {
        UserStatus[] statuses = switch (role) {
            case STUDENT -> new UserStatus[]{EMAIL_APPROVAL_PENDING, ACTIVE};
            case DRIVER -> new UserStatus[]{ADMIN_APPROVAL_PENDING, ACTIVE};
            case ADMIN, STAFF -> new UserStatus[]{ACTIVE, INACTIVE};
        };
        return statuses[faker.random().nextInt(statuses.length)];
    }

    private String generateSouthAfricanPhoneNumber() {
        String[] prefixes = {"081", "082", "076", "072", "079", "083"};
        String selectedPrefix = prefixes[faker.random().nextInt(prefixes.length)];
        String restOfNumber = String.format("%07d", faker.number().numberBetween(1000000, 9999999));
        return selectedPrefix + restOfNumber.substring(0, 3) + restOfNumber.substring(3);
    }
}