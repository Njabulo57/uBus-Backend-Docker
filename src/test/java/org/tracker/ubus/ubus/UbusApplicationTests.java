package org.tracker.ubus.ubus;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class UbusApplicationTests {


    @BeforeAll
    static void loadEnvironmentVariables() {

        //injecting it from the project root
        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .load();

        //injecting it into the system properties for spring to use
        dotenv.entries().forEach(entry ->
                System.setProperty(entry.getKey(), entry.getValue())
        );

        System.out.println("DB_USERNAME loaded: " + System.getProperty("DB_USERNAME"));
        System.out.println("DB_pass loaded: " + System.getProperty("DB_PASSWORD"));
    }


    @Test
    void contextLoads() {
    }

}
