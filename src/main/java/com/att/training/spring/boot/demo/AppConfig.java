package com.att.training.spring.boot.demo;

import com.att.training.spring.boot.demo.user.UserConfiguration;
import com.google.common.util.concurrent.Uninterruptibles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.format.number.NumberFormatAnnotationFormatterFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.stream.IntStream;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.stream.Collectors.toList;

@Configuration
@EnableAspectJAutoProxy
@EnableAsync
@Slf4j
public class AppConfig {

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
    Clock clock() {
        return Clock.systemUTC();
    }

    //@Bean
    public FormattingConversionService conversionService() {
        DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService(false);
        conversionService.addFormatterForFieldAnnotation(new NumberFormatAnnotationFormatterFactory());
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        registrar.registerFormatters(conversionService);

        return conversionService;
    }

    @Bean
    public Executor taskExecutor() {
        var coreCount = Runtime.getRuntime().availableProcessors();
        var taskExecutor = new TaskExecutorBuilder()
                .corePoolSize(coreCount)
                .maxPoolSize(coreCount)
                .threadNamePrefix("file-download-")
                .build();

        taskExecutor.setDaemon(true);
        taskExecutor.setThreadFactory(new ExceptionHandlingThreadFactory(taskExecutor,
                (t, e) -> log.error("An error occurred", e)));
        return taskExecutor;
    }

    @Bean
    CommandLineRunner asyncTester(AsyncRunner runner) {
        return runner::runAsync;
    }

    @Bean
    CommandLineRunner completableFutures(Executor executor) {
        return args -> {
            List<CompletableFuture<String>> futures = IntStream.range(1, 10)
                                                               .mapToObj(i -> asyncTask(i, executor))
                                                               .collect(toList());

            merge(futures).thenAccept(results -> log.info("Got the following results: {}", results));
            executor.execute(() -> {throw new RuntimeException("Boom!");});
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

@RequiredArgsConstructor
class ExceptionHandlingThreadFactory implements ThreadFactory {

    private final ThreadFactory backingThreadFactory;
    private final Thread.UncaughtExceptionHandler handler;

    @Override
    public Thread newThread(Runnable r) {
        var thread = backingThreadFactory.newThread(r);
        thread.setUncaughtExceptionHandler(handler);
        return thread;
    }
}
