package com.att.training.spring.boot.demo;

import com.google.common.util.concurrent.Uninterruptibles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

@Slf4j
@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = {"taskExecutor", "cpuTaskExecutor"})
    public Executor cpuTaskExecutor() {
        var coreCount = Runtime.getRuntime().availableProcessors();
        return buildExecutor(coreCount, "cpu-pool");
    }

    @Bean
    public Executor ioTaskExecutor() {
        var coreCount = Runtime.getRuntime().availableProcessors() * 64;
        return buildExecutor(coreCount, "io-pool-");
    }

    private ThreadPoolTaskExecutor buildExecutor(int coreCount, String prefix) {
        var taskExecutor = new TaskExecutorBuilder()
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
    CommandLineRunner completableFutures(Executor ioTaskExecutor) {
        return args -> {
            List<CompletableFuture<String>> futures = IntStream.range(1, 10)
                    .mapToObj(i -> asyncTask(i, ioTaskExecutor))
                    .collect(toList());

            merge(futures).thenAccept(results -> log.info("Got the following results: {}", results));
            ioTaskExecutor.execute(() -> {throw new IllegalStateException("Boom!");});
        };
    }

    private static final Random random = new Random();

    private CompletableFuture<String> asyncTask(int i, Executor executor) {
        return supplyAsync(() -> sleepAndReturn(random.nextInt(1000), Integer.toString(i)), executor)
                .exceptionally(Throwable::getMessage);
    }

    private static <T> CompletableFuture<List<T>> merge(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        return all.thenApply(ignore -> futures.stream()
                .map(CompletableFuture::join)
                .collect(toList()));
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
        public Thread newThread(@NonNull Runnable r) {
            var thread = backingThreadFactory.newThread(r);
            thread.setUncaughtExceptionHandler(handler);
            return thread;
        }
    }
}
