package org.tracker.ubus.ubus.Configuration.Scheduling;


import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class for enabling and setting up scheduling in the application.
 *
 * This class is annotated with {@code @Configuration} to indicate that it is a
 * configuration class that defines beans for the application context. It is also
 * annotated with {@code @EnableScheduling} to enable Spring's scheduled task execution
 * capability.
 *
 * By adding this class to the application's context, it allows the use of scheduled
 * tasks annotated with {@code @Scheduled} within the application.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
