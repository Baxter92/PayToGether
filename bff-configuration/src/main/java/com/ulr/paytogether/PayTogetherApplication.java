package com.ulr.paytogether;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principale de l'application PayToGether
 * Point d'entrée de l'application Spring Boot
 */
@SpringBootApplication(scanBasePackages = "com.ulr.paytogether")
public class PayTogetherApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayTogetherApplication.class, args);
    }
}
