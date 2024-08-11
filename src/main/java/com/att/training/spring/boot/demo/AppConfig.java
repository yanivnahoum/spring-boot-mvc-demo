package com.att.training.spring.boot.demo;

import com.att.training.spring.boot.demo.user.UserConfiguration;
import com.google.common.util.concurrent.Uninterruptibles;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

@Configuration
@EnableAspectJAutoProxy
@Slf4j
class AppConfig {

    @Bean
    CommandLineRunner configurationPrinter(UserConfiguration userConfiguration) {
        return args -> log.info("#configurationPrinter - {}", userConfiguration);
    }

    @Bean
    CommonsRequestLoggingFilter requestLoggingFilter() {
        var loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(200);
        return loggingFilter;
    }


    @Bean
    Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setDaemon(true);
        executor.setThreadNamePrefix("file-download-");
        return executor;
    }

    @Bean
    AsyncConfigurer asyncConfigurer() {
        return new AsyncConfigurer() {
            @Override
            public Executor getAsyncExecutor() {
                return taskExecutor();
            }

            @Override
            public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
                return (ex, method, params) -> log.error("An error occurred in method {} invoked with params: {}: ", method
                        .getName(), params, ex);
            }
        };

    }

    @Bean
    CommandLineRunner asyncRunner(TaskExecutor executor) {
        return args -> {
            List<CompletableFuture<String>> futures = IntStream.range(1, 10)
                                                               .boxed()
                                                               .map((Integer i) -> asyncTask(i, executor))
                                                               .collect(toList());

            merge(futures).thenAccept(results -> log.info("Got the following results: {}", results));
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
            throw new RuntimeException("Boom!");
        }
        log.info("[{}] - Slept {}ms, now returning {}", Thread.currentThread().getName(), ms, obj);
        return obj;
    }
}
