package com.ulr.paytogether;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principale de l'application PayToGether
 * Point d'entre de l'application Spring Boot
 */
@SpringBootApplication(scanBasePackages = "com.ulr.paytogether")
@EnableCaching
@EnableScheduling
public class PayTogetherApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayTogetherApplication.class, args);
    }
}
