package com.ulr.paytogether.bff.eventdispatcher.dispatcher;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration Spring pour le module event-dispatcher
 */
@Configuration
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = "com.ulr.paytogether.bff.eventdispatcher")
@EnableJpaRepositories(basePackages = "com.ulr.paytogether.bff.eventdispatcher.repository")
public class EventDispatcherConfiguration {
}

