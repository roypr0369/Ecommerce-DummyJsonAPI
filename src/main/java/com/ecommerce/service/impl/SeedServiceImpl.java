package com.ecommerce.service.impl;

import com.ecommerce.dto.DummyJsonResponse;
import com.ecommerce.dto.DummyProduct;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.ISeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Concrete implementation of ISeedService.
 * With Debezium CDC in place:
 *   - This class seeds MySQL ONLY.
 *   - Every INSERT written to MySQL triggers a Debezium CDC event on Kafka.
 *   - DebeziumProductConsumer reads those events and indexes to Elasticsearch.
 *   - Elasticsearch is no longer written to directly from the seed pipeline.
 *
 * This means Elasticsearch is always derived from MySQL — never written to
 * independently. MySQL is the single source of truth.
 *
 */
@Service
@Order(2)
public class SeedServiceImpl implements ISeedService {

    private static final Logger log = LoggerFactory.getLogger(SeedServiceImpl.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository  productRepository;
    private final ProductMapper      productMapper;
    private final RestTemplate       restTemplate;

    @Value("${app.dummyjson-url}")
    private String dummyJsonUrl;

    public SeedServiceImpl(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           ProductMapper productMapper,
                           RestTemplate restTemplate) {
        this.categoryRepository = categoryRepository;
        this.productRepository  = productRepository;
        this.productMapper      = productMapper;
        this.restTemplate       = restTemplate;
    }

    // ── Template Method skeleton ──────────────────────────────────────────────

    @Override
    public void seed() {
        if (isAlreadySeeded()) {
            log.info("[Seed] Products already present in MySQL — skipping.");
            return;
        }
        log.info("[Seed] Starting MySQL ingestion pipeline…");

        List<DummyProduct> products = fetchFromDummyJson();
        if (products.isEmpty()) {
            log.warn("[Seed] No products fetched — aborting.");
            return;
        }

        saveToMySQL(products);

        log.info("[Seed] ✅ MySQL seeding complete — {} products written.", products.size());
        log.info("[Seed] Debezium will publish CDC events → Kafka → Elasticsearch.");
    }

    // ── Step 0: Idempotency guard ─────────────────────────────────────────────

    protected boolean isAlreadySeeded() {
        return productRepository.count() > 0;
    }

    // ── Step 1: Fetch from external API ──────────────────────────────────────

    protected List<DummyProduct> fetchFromDummyJson() {
        log.info("[Seed] Fetching from {} …", dummyJsonUrl);
        DummyJsonResponse response = restTemplate.getForObject(dummyJsonUrl, DummyJsonResponse.class);
        List<DummyProduct> products = (response != null && response.getProducts() != null)
                ? response.getProducts()
                : List.of();
        log.info("[Seed] Fetched {} products.", products.size());
        return products;
    }

    // ── Step 2: Persist to MySQL ──────────────────────────────────────────────

    /**
     * Writes products to MySQL transactionally.
     * Each INSERT here will be captured by Debezium as a CDC event and
     * forwarded to Kafka → DebeziumProductConsumer → Elasticsearch.
     * No ES code here — that concern is fully delegated to the consumer.
     */
    @Transactional
    protected void saveToMySQL(List<DummyProduct> dummyProducts) {
        log.info("[MySQL] Persisting {} products…", dummyProducts.size());
        for (DummyProduct dp : dummyProducts) {
            if (productRepository.existsById(dp.getId())) continue;

            // Find-or-Create pattern for Category
            Category category = categoryRepository.findByName(dp.getCategory())
                    .orElseGet(() -> categoryRepository.save(
                            new Category(dp.getCategory(), slugify(dp.getCategory()))
                    ));

            Product product = productMapper.toEntity(dp, category);
            productRepository.save(product);
        }
        log.info("[MySQL] Done ✓");
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String slugify(String name) {
        if (name == null) return "uncategorized";
        return name.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}