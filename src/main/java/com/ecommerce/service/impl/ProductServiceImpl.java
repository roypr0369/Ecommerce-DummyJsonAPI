package com.ecommerce.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.ecommerce.dto.ApiListResponse;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.document.ProductDocument;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.service.IProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Concrete implementation of IProductService.
 *
 * Design Patterns / Principles:
 *
 *  - Strategy Pattern: the routing between MySQL and Elasticsearch is a
 *    runtime strategy decision made in the controller based on which query
 *    parameters are present. Each strategy (listProducts, filterByCategory,
 *    searchProducts) is encapsulated in its own method here.
 *
 *  - Facade Pattern: this class acts as a Facade over the ProductRepository
 *    and ElasticsearchOperations sub-systems. Controllers call one clean
 *    service method instead of orchestrating both systems themselves.
 *
 *  - SRP: this class handles product queries only. Mapping is delegated to
 *    ProductMapper. Seeding is in SeedServiceImpl.
 *
 *  - DIP: depends on the IProductService interface (via @Service) and
 *    injects ProductMapper/Repository through the constructor.
 */
@Service
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                               ElasticsearchOperations elasticsearchOperations,
                               ProductMapper productMapper) {
        this.productRepository       = productRepository;
        this.elasticsearchOperations = elasticsearchOperations;
        this.productMapper           = productMapper;
    }

    // ── Strategy: list all (MySQL) ────────────────────────────────────────────

    @Override
    public ApiListResponse<ProductResponse> listProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Product> resultPage = productRepository.findAll(pageable);
        List<ProductResponse> data = resultPage.getContent().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        return new ApiListResponse<>(resultPage.getTotalElements(), page, size, data);
    }

    // ── Strategy: filter by category (MySQL) ─────────────────────────────────

    @Override
    public ApiListResponse<ProductResponse> filterByCategory(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Product> resultPage = productRepository.findByCategoryNameOrSlug(category, pageable);
        List<ProductResponse> data = resultPage.getContent().stream()
                .map(productMapper::toResponse)
                .collect(Collectors.toList());
        return new ApiListResponse<>(resultPage.getTotalElements(), page, size, data);
    }

    // ── Strategy: get by ID (MySQL) ───────────────────────────────────────────

    @Override
    public ProductResponse getById(Integer id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return productMapper.toResponse(product);
    }

    // ── Strategy: full-text search (Elasticsearch) ───────────────────────────

    /**
     * Builds a bool query:
     *  must   → multi_match across title^3, description, brand^2, tags^2
     *  filter → optional term filter on category keyword field
     *
     * fuzziness("auto") handles typos gracefully without degrading precision.
     * Category is applied as a filter (not must) so it doesn't affect relevance
     * scoring — only narrows the result set.
     */
    @Override
    public List<ProductResponse> searchProducts(String queryText, String category,
                                                int page, int size) {
        BoolQuery.Builder boolBuilder = new BoolQuery.Builder();

        boolBuilder.must(m -> m.multiMatch(mm -> mm
                .query(queryText)
                .fields("title^3", "description", "brand^2", "tags^2")
                .fuzziness("auto")
        ));

        if (category != null && !category.isBlank()) {
            boolBuilder.filter(f -> f.term(t -> t
                    .field("category")
                    .value(category)
            ));
        }

        Query esQuery = Query.of(q -> q.bool(boolBuilder.build()));

        NativeQuery nativeQuery = NativeQuery.builder()
                .withQuery(esQuery)
                .withPageable(PageRequest.of(page, size))
                .build();

        SearchHits<ProductDocument> hits =
                elasticsearchOperations.search(nativeQuery, ProductDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> productMapper.toResponse(hit.getContent()))
                .collect(Collectors.toList());
    }
}
