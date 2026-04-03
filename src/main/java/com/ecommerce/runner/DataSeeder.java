package com.ecommerce.runner;

import com.ecommerce.service.ISeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Triggers the data seeding pipeline once the Spring application context
 * is fully loaded and the server is ready to accept connections.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final ISeedService seedService;

    public DataSeeder(ISeedService seedService) {
        this.seedService = seedService;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("[DataSeeder] Application ready — initiating seed check…");
        try {
            seedService.seed();
        } catch (Exception e) {
            log.error("[DataSeeder] Seed failed: {}", e.getMessage(), e);
        }
    }
}
