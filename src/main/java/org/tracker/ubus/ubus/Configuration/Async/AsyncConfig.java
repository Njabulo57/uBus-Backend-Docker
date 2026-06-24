package org.tracker.ubus.ubus.Configuration.Async;

import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Configuration class that enables asynchronous method execution by implementing the {@link AsyncConfigurer} interface.
 * This class customizes the {@link Executor} used for running asynchronous tasks.
 */
@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    /**
     * Provides the {@link Executor} to be used for asynchronous method execution.
     * This implementation returns a virtual thread-per-task executor.
     *
     * @return an {@code Executor} instance for asynchronous execution, or {@code null} if no executor is configured
     */
    @Override
    public @Nullable Executor getAsyncExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
