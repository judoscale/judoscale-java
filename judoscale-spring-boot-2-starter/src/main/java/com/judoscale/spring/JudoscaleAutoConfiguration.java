package com.judoscale.spring;

import com.judoscale.core.MetricsStore;
import com.judoscale.core.UtilizationTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ScheduledFuture;

/**
 * Auto-configuration for Judoscale Spring Boot integration.
 * Automatically registers the filter and reporter when the starter is on the classpath.
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "judoscale.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JudoscaleConfig.class)
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

        FilterRegistrationBean<JudoscaleFilter> registration = new FilterRegistrationBean<JudoscaleFilter>();
        registration.setFilter(new JudoscaleFilter(metricsStore, config, utilizationTracker));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("judoscaleFilter");

        return registration;
    }

    /**
     * Dedicated task scheduler for Judoscale to avoid conflicts with application scheduling.
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = "judoscaleTaskScheduler")
    public ThreadPoolTaskScheduler judoscaleTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("judoscale-");
        scheduler.setDaemon(true);
        scheduler.initialize();
        return scheduler;
    }

    /**
     * Scheduler component that triggers metric reporting.
     * Uses programmatic scheduling instead of @Scheduled for Spring Boot 2.6 compatibility.
     */
    @Bean
    @ConditionalOnMissingBean(JudoscaleScheduler.class)
    public JudoscaleScheduler judoscaleScheduler(
            JudoscaleReporter reporter,
            JudoscaleConfig config,
            TaskScheduler judoscaleTaskScheduler) {
        return new JudoscaleScheduler(reporter, config, judoscaleTaskScheduler);
    }

    /**
     * Inner class to handle scheduling programmatically for Spring Boot 2.6 compatibility.
     */
    public static class JudoscaleScheduler {

        private final JudoscaleReporter reporter;
        private final JudoscaleConfig config;
        private final TaskScheduler taskScheduler;
        private ScheduledFuture<?> scheduledTask;

        public JudoscaleScheduler(JudoscaleReporter reporter, JudoscaleConfig config, TaskScheduler taskScheduler) {
            this.reporter = reporter;
            this.config = config;
            this.taskScheduler = taskScheduler;
        }

        @PostConstruct
        public void init() {
            reporter.start();
            long intervalMs = config.getReportIntervalSeconds() * 1000L;
            scheduledTask = taskScheduler.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    reporter.reportMetrics();
                }
            }, intervalMs);
        }

        @PreDestroy
        public void destroy() {
            if (scheduledTask != null) {
                scheduledTask.cancel(false);
            }
        }
    }
}
