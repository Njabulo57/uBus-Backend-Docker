package org.tracker.ubus.ubus.TestDataLoader;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.tracker.ubus.ubus.Components.User.Entity.User;
import org.tracker.ubus.ubus.Components.User.Enum.UserRole;
import org.tracker.ubus.ubus.Components.User.Enum.UserStatus;
import org.tracker.ubus.ubus.Components.User.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
public class UserDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        if (userRepository.count() == 0) {

            User student = new User();
            student.setFirstname("firstname");
            student.setLastname("lastname");
            student.setEmail("student@test.com");
            student.setPassword(passwordEncoder.encode("student123"));
            student.setRole(UserRole.STUDENT);
            student.setStatus(UserStatus.EMAIL_APPROVAL_PENDING);

            User admin = new User();
            admin.setFirstname("firstname");
            admin.setLastname("lastname");
            admin.setRole(UserRole.ADMIN);
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setStatus(UserStatus.ACTIVE);
            admin.setRole(UserRole.ADMIN);

            User driver = new User();
            driver.setFirstname("firstname");
            driver.setLastname("lastname");
            driver.setEmail("driver@test.com");
            driver.setStatus(UserStatus.ACTIVE);
            driver.setPassword(passwordEncoder.encode("driver123"));
            driver.setRole(UserRole.DRIVER);

            User staff = new User();
            staff.setFirstname("firstname");
            staff.setLastname("lastname");
            staff.setEmail("staff@test.com");
            staff.setStatus(UserStatus.ACTIVE);
            staff.setPassword(passwordEncoder.encode("staff123"));
            staff.setRole(UserRole.STAFF);

            userRepository.save(student);
            userRepository.save(admin);
            userRepository.save(driver);
            userRepository.save(staff);

            System.out.println("Test users loaded");
        }
    }
}