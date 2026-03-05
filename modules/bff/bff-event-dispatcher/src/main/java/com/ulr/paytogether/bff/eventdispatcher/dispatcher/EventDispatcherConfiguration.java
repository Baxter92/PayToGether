package com.ulr.paytogether.bff.eventdispatcher.dispatcher;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration Spring pour le module event-dispatcher
 * Note: @EnableJpaRepositories est configuré dans JpaConfiguration (bff-configuration)
 */
@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = "com.ulr.paytogether.bff.eventdispatcher")
public class EventDispatcherConfiguration {
}

