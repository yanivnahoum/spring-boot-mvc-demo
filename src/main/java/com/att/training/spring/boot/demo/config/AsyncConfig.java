package com.att.training.spring.boot.demo.config;

import com.google.common.util.concurrent.Uninterruptibles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
@EnableAsync
@Configuration(proxyBeanMethods = false)
public class AsyncConfig {
    @Bean
    TaskDecorator taskDecorator() {
        return runnable -> () -> {
            try {
                log.trace("#Decorator - start: executing task on thread: {}", Thread.currentThread().getName());
                runnable.run();
            } finally {
                log.trace("#Decorator - end: executed task on thread: {}", Thread.currentThread().getName());
            }
        };
    }

    @Bean({"ioTaskExecutor", TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME})
    public TaskExecutor ioTaskExecutor(ThreadPoolTaskExecutorBuilder builder) {
        var coreCount = Runtime.getRuntime().availableProcessors() * 64;
        return buildExecutor(builder, coreCount, "io-pool-");
    }

    @Cpu
    @Bean(defaultCandidate = false)
    public TaskExecutor cpuTaskExecutor(ThreadPoolTaskExecutorBuilder builder) {
        var coreCount = Runtime.getRuntime().availableProcessors();
        return buildExecutor(builder, coreCount, "cpu-pool");
    }

    private ThreadPoolTaskExecutor buildExecutor(ThreadPoolTaskExecutorBuilder builder, int coreCount, String prefix) {
        var taskExecutor = builder
                .corePoolSize(coreCount)
                .maxPoolSize(coreCount)
                .threadNamePrefix(prefix)
                .build();

        taskExecutor.setDaemon(true);
        taskExecutor.setThreadFactory(new ExceptionHandlingThreadFactory(taskExecutor,
                (t, e) -> log.error("An error occurred", e)));
        return taskExecutor;
    }

    @Bean
    CommandLineRunner asyncTester1(AsyncRunner1 runner) {
        return runner::runAsync;
    }

    @Bean
    CommandLineRunner asyncTester2(AsyncRunner2 runner) {
        return runner::runAsync;
    }

    @Bean
    CommandLineRunner completableFutures(@Cpu Executor executor) {
        return args -> {
            List<CompletableFuture<String>> futures = IntStream.range(1, 10)
                    .mapToObj(i -> asyncTask(i, executor))
                    .toList();

            merge(futures).thenAccept(results -> log.info("Got the following results: {}", results)).join();
            executor.execute(() -> {throw new IllegalStateException("Boom!");});
        };
    }

    private static final Random random = new Random();

    private CompletableFuture<String> asyncTask(int i, Executor executor) {
        return supplyAsync(() -> sleepAndReturn(random.nextInt(1000), Integer.toString(i)), executor)
                .exceptionally(Throwable::getMessage);
    }

    private static <T> CompletableFuture<List<T>> merge(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0]));
        return all.thenApply(ignore -> futures.stream()
                .map(CompletableFuture::join)
                .toList());
    }

    private static <T> T sleepAndReturn(int ms, T obj) {
        Uninterruptibles.sleepUninterruptibly(ms, MILLISECONDS);
        if (ms > 750) {
            throw new IllegalStateException("Boom!");
        }
        log.info("[{}] - Slept {}ms, now returning {}", Thread.currentThread().getName(), ms, obj);
        return obj;
    }

    @RequiredArgsConstructor
    static class ExceptionHandlingThreadFactory implements ThreadFactory {
        private final ThreadFactory backingThreadFactory;
        private final Thread.UncaughtExceptionHandler handler;

        @Override
        public Thread newThread(Runnable r) {
            var thread = backingThreadFactory.newThread(r);
            thread.setUncaughtExceptionHandler(handler);
            return thread;
        }
    }

    @Retention(RUNTIME)
    @Target({METHOD, PARAMETER})
    @Qualifier("cpu")
    @interface Cpu {}
}
