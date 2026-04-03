package com.ecommerce.service.impl;

import com.ecommerce.document.ProductDocument;
import com.ecommerce.dto.DummyJsonResponse;
import com.ecommerce.dto.DummyProduct;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.model.Category;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.ProductSearchRepository;
import com.ecommerce.service.ISeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class SeedServiceImpl implements ISeedService {

    private static final Logger log = LoggerFactory.getLogger(SeedServiceImpl.class);

    private final CategoryRepository      categoryRepository;
    private final ProductRepository       productRepository;
    private final ProductSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductMapper           productMapper;
    private final RestTemplate            restTemplate;

    @Value("${app.dummyjson-url}")
    private String dummyJsonUrl;

    public SeedServiceImpl(CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           ProductSearchRepository searchRepository,
                           ElasticsearchOperations elasticsearchOperations,
                           ProductMapper productMapper,
                           RestTemplate restTemplate) {
        this.categoryRepository    = categoryRepository;
        this.productRepository     = productRepository;
        this.searchRepository      = searchRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.productMapper         = productMapper;
        this.restTemplate          = restTemplate;
    }

    // ── Template Method: fixed algorithm skeleton ─────────────────────────────

    @Override
    public void seed() {
        if (isAlreadySeeded()) {
            log.info("[Seed] Data already present — skipping.");
            return;
        }
        log.info("[Seed] Starting data ingestion pipeline…");

        List<DummyProduct> products = fetchFromDummyJson();   // Step 1
        if (products.isEmpty()) {
            log.warn("[Seed] No products fetched — aborting.");
            return;
        }

        saveToMySQL(products);                                 // Step 2
        createEsIndexIfMissing();                              // Step 3
        indexToElasticsearch(products);                        // Step 4

        log.info("[Seed] ✅ Pipeline complete — {} products ingested.", products.size());
    }

    // ── Step 0: Idempotency guard ─────────────────────────────────────────────

    protected boolean isAlreadySeeded() {
        return productRepository.count() > 0;
    }

    // ── Step 1: Fetch ─────────────────────────────────────────────────────────

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
     * @Transactional: if any product insert fails, the entire batch rolls back.
     * Categories are upserted first (find-or-create pattern).
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

    // ── Step 3: Create ES index ───────────────────────────────────────────────

    /**
     * Spring Data Elasticsearch derives the mapping from @Field annotations
     * on ProductDocument — no hand-written JSON mapping needed.
     */
    protected void createEsIndexIfMissing() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(ProductDocument.class);
        if (!indexOps.exists()) {
            indexOps.createWithMapping();
            log.info("[ES] Index 'products' created with mapping ✓");
        } else {
            log.info("[ES] Index 'products' already exists — skipping.");
        }
    }

    // ── Step 4: Bulk index to Elasticsearch ──────────────────────────────────

    protected void indexToElasticsearch(List<DummyProduct> dummyProducts) {
        log.info("[ES] Indexing {} documents…", dummyProducts.size());
        List<ProductDocument> docs = new ArrayList<>();
        for (DummyProduct dp : dummyProducts) {
            docs.add(productMapper.toDocument(dp));
        }
        // saveAll() calls the ES bulk API — far faster than single-document saves
        searchRepository.saveAll(docs);
        log.info("[ES] Indexed {} documents ✓", docs.size());
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private String slugify(String name) {
        if (name == null) return "uncategorized";
        return name.toLowerCase()
                   .replaceAll("[^a-z0-9]+", "-")
                   .replaceAll("^-|-$", "");
    }
}
