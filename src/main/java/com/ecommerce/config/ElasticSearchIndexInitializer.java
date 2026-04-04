package com.ecommerce.config;

import com.ecommerce.document.ProductDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Component;


/**
 * Ensures the Elasticsearch 'products' index exists with the correct mapping
 * before any Debezium CDC events arrive and before the DataSeeder runs.
 *
 * Design Principles:
 *  - SRP: only responsible for ES index lifecycle — nothing else.
 *  - @Order(1): runs FIRST among all ApplicationRunners so the index is
 *    always ready before the Kafka consumer starts receiving events.
 *
 * Previously this logic lived inside SeedServiceImpl. It was extracted here
 * because in the CDC architecture, ES index creation is a startup concern
 * independent of data seeding — the index must exist even before the first
 * Debezium event arrives.
 */

@Component
@Order(value = 1)
public class ElasticSearchIndexInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(ElasticSearchIndexInitializer.class);

    private final ElasticsearchOperations elasticsearchOperations;

    public ElasticSearchIndexInitializer(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("[ES] Checking products index…");
        IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
        if (!indexOps.exists()) {
            indexOps.createWithMapping();
            log.info("[ES] Products index created with mapping ✓");
        } else {
            log.info("[ES] Products index already exists — skipping creation.");
        }
    }
}
