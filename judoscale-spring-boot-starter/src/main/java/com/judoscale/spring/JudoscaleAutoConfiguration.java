package com.judoscale.spring;

import com.judoscale.core.MetricsStore;
import com.judoscale.core.UtilizationTracker;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Auto-configuration for Judoscale Spring Boot integration.
 * Automatically registers the filter and reporter when the starter is on the classpath.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "judoscale.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JudoscaleConfig.class)
@EnableScheduling
public class JudoscaleAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JudoscaleAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(MetricsStore.class)
    public MetricsStore judoscaleMetricsStore() {
        return new MetricsStore();
    }

    @Bean
    @ConditionalOnMissingBean(UtilizationTracker.class)
    public UtilizationTracker judoscaleUtilizationTracker() {
        return new UtilizationTracker();
    }

    @Bean
    @ConditionalOnMissingBean(JudoscaleApiClient.class)
    public JudoscaleApiClient judoscaleApiClient(JudoscaleConfig config) {
        return new JudoscaleApiClient(config);
    }

    @Bean
    @ConditionalOnMissingBean(JudoscaleReporter.class)
    public JudoscaleReporter judoscaleReporter(
            MetricsStore metricsStore,
            JudoscaleApiClient apiClient,
            JudoscaleConfig config,
            UtilizationTracker utilizationTracker) {
        return new JudoscaleReporter(metricsStore, apiClient, config, utilizationTracker);
    }

    @Bean
    @ConditionalOnMissingBean(name = "judoscaleFilter")
    public FilterRegistrationBean<JudoscaleFilter> judoscaleFilter(
            MetricsStore metricsStore,
            JudoscaleConfig config,
            UtilizationTracker utilizationTracker) {

        FilterRegistrationBean<JudoscaleFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JudoscaleFilter(metricsStore, config, utilizationTracker));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("judoscaleFilter");

        return registration;
    }

    /**
     * Dedicated task scheduler for Judoscale to avoid conflicts with application scheduling.
     */
    @Bean
    @ConditionalOnMissingBean(name = "judoscaleTaskScheduler")
    public TaskScheduler judoscaleTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("judoscale-");
        scheduler.setDaemon(true);
        return scheduler;
    }

    /**
     * Scheduler component that triggers metric reporting.
     */
    @Bean
    @ConditionalOnMissingBean(JudoscaleScheduler.class)
    public JudoscaleScheduler judoscaleScheduler(JudoscaleReporter reporter) {
        return new JudoscaleScheduler(reporter);
    }

    /**
     * Inner class to handle scheduling, allowing for proper @Scheduled annotation processing.
     */
    public static class JudoscaleScheduler {

        private final JudoscaleReporter reporter;

        public JudoscaleScheduler(JudoscaleReporter reporter) {
            this.reporter = reporter;
        }

        @PostConstruct
        public void init() {
            reporter.start();
        }

        @Scheduled(fixedRateString = "${judoscale.report-interval-seconds:10}000", scheduler = "judoscaleTaskScheduler")
        public void reportMetrics() {
            reporter.reportMetrics();
        }
    }
}
