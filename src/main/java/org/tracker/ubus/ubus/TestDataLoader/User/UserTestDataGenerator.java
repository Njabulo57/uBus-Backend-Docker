package org.tracker.ubus.ubus.TestDataLoader.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.Users.User.Entity.User;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.Users.User.Repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;

import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserRole.*;
import static org.tracker.ubus.ubus.Components.Users.User.Enum.UserStatus.*;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class UserTestDataGenerator implements CommandLineRunner {

    private static final String[] SA_PREFIXES = {"081", "082", "076", "072", "079", "083"};

    private final Faker faker;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Defaults — override via application.properties or CLI args
    @Value("${seed.users.students:50000}") private int defaultStudents;
    @Value("${seed.users.admins:70}")    private int defaultAdmins;
    @Value("${seed.users.staff:10000}")    private int defaultStaff;
    @Value("${seed.users.drivers:100}")   private int defaultDrivers;
    @Value("${seed.users.force:false}")  private boolean defaultForce;

    @Override
    public void run(String... args) {
        Counts c = Counts.from(args, defaultStudents, defaultAdmins, defaultStaff, defaultDrivers, defaultForce);

        if (c.total() == 0) {
            log.info("Seeder: all counts are 0, nothing to do");
            return;
        }
        if (!c.force && userRepository.count() > 0) {
            log.info("Users already exist, skipping. Pass --force=true to seed anyway.");
            return;
        }

        log.info("Seeding -> students={}, admins={}, staff={}, drivers={} (total {})",
                c.students, c.admins, c.staff, c.drivers, c.total());

        long t0 = System.currentTimeMillis();

        // Encode each password ONCE per role used
        String studentPwd = c.students > 0 ? passwordEncoder.encode("student@123") : null;
        String adminPwd   = c.admins   > 0 ? passwordEncoder.encode("admin@123")   : null;
        String staffPwd   = c.staff    > 0 ? passwordEncoder.encode("staff@123")   : null;
        String driverPwd  = c.drivers  > 0 ? passwordEncoder.encode("driver@2025") : null;

        List<User> all = new ArrayList<>(c.total());
        addMany(all, c.students, i -> student(i, studentPwd));
        addMany(all, c.admins,   i -> admin(i, adminPwd));
        addMany(all, c.staff,    i -> staff(i, staffPwd));
        addMany(all, c.drivers,  i -> driver(i, driverPwd));

        userRepository.saveAll(all);
        userRepository.flush();

        log.info("Seeded {} users in {} ms", all.size(), System.currentTimeMillis() - t0);
    }

    private void addMany(List<User> out, int count, IntFunction<User> builder) {
        for (int i = 1; i <= count; i++) out.add(builder.apply(i));
    }

    private User student(int n, String pwd) {
        return base(pwd, STUDENT)
                .email("2024" + String.format("%05d", n) + "@student.ubus.ac.za")
                .build();
    }

    private User admin(int n, String pwd) {
        return base(pwd, ADMIN)
                .email(String.format("admin%03d@admin.ubus.za", n))
                .build();
    }

    private User staff(int n, String pwd) {
        return base(pwd, STAFF)
                .email(String.format("staff%03d@ubus.ac.za", n))
                .build();
    }

    private User driver(int n, String pwd) {
        return base(pwd, DRIVER)
                .email(String.format("driver%03d@ubus.co.za", n))
                .build();
    }

    private User.UserBuilder base(String pwd, UserRole role) {
        return User.builder()
                .firstname(faker.name().firstName())
                .lastname(faker.name().lastName())
                .password(pwd)
                .phoneNumber(saPhone())
                .role(role)
                .status(randomStatus(role));
    }

    private UserStatus randomStatus(UserRole role) {
        UserStatus[] s = switch (role) {
            case STUDENT      -> new UserStatus[]{EMAIL_APPROVAL_PENDING, ACTIVE};
            case DRIVER       -> new UserStatus[]{ADMIN_APPROVAL_PENDING, ACTIVE};
            case ADMIN, STAFF -> new UserStatus[]{ACTIVE, INACTIVE};
        };
        return s[ThreadLocalRandom.current().nextInt(s.length)];
    }

    private String saPhone() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        return SA_PREFIXES[r.nextInt(SA_PREFIXES.length)]
                + String.format("%07d", r.nextInt(1_000_000, 10_000_000));
    }

    /** Parses --students=N --admins=N --staff=N --drivers=N --force=true from CLI, falling back to defaults. */
    private record Counts(int students, int admins, int staff, int drivers, boolean force) {
        int total() { return students + admins + staff + drivers; }

        static Counts from(String[] args, int ds, int da, int dst, int dd, boolean dForce) {
            int students = ds, admins = da, staff = dst, drivers = dd;
            boolean force = dForce;
            for (String a : args) {
                if (!a.startsWith("--")) continue;
                String[] kv = a.substring(2).split("=", 2);
                if (kv.length != 2) continue;
                switch (kv[0]) {
                    case "students" -> students = parse(kv[1], students);
                    case "admins"   -> admins   = parse(kv[1], admins);
                    case "staff"    -> staff    = parse(kv[1], staff);
                    case "drivers"  -> drivers  = parse(kv[1], drivers);
                    case "force"    -> force    = Boolean.parseBoolean(kv[1]);
                }
            }
            return new Counts(Math.max(0, students), Math.max(0, admins),
                    Math.max(0, staff),    Math.max(0, drivers), force);
        }

        private static int parse(String s, int fallback) {
            try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
        }
    }
}
