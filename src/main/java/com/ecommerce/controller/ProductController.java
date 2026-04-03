package com.ecommerce.controller;

import com.ecommerce.dto.ApiListResponse;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.service.IProductService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for product endpoints.
 * Endpoints:
 *   GET /products                      → paginated list (MySQL)
 *   GET /products?query={q}            → full-text search (ES)
 *   GET /products?category={cat}       → category filter (MySQL)
 *   GET /products?query={q}&category={cat} → search within category (ES)
 *   GET /products/{id}                 → single product (MySQL)
 */
@RestController
@RequestMapping("/products")
@Validated
public class ProductController {

    private final IProductService productService;

    public ProductController(IProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "30") @Min(1) @Max(100) int size
    ) {
        if (query != null && !query.isBlank()) {
            // Full-text search via Elasticsearch (with optional category filter)
            List<ProductResponse> results =
                    productService.searchProducts(query, category, page, size);
            return ResponseEntity.ok(results);
        }

        if (category != null && !category.isBlank()) {
            // Category filter via MySQL
            ApiListResponse<ProductResponse> result =
                    productService.filterByCategory(category, page, size);
            return ResponseEntity.ok(result);
        }

        // Default: paginated product list from MySQL
        ApiListResponse<ProductResponse> result = productService.listProducts(page, size);
        return ResponseEntity.ok(result);
    }

    // ── GET /products/{id} ────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Integer id) {
        // ResourceNotFoundException is thrown by the service and handled
        // globally by GlobalExceptionHandler — no try/catch needed here.
        return ResponseEntity.ok(productService.getById(id));
    }
}
