package com.ecommerce.service;

import com.ecommerce.dto.ApiListResponse;
import com.ecommerce.dto.ProductResponse;

import java.util.List;

/**
 * Contract for product-related operations.
 *
 * Design Principles:
 *  - ISP  (Interface Segregation): this interface covers ONLY product queries;
 *    category operations live in ICategoryService.
 *  - DIP  (Dependency Inversion): controllers depend on this abstraction,
 *    not on the concrete implementation class.
 *  - OCP  (Open/Closed): new implementations (e.g. a cached variant) can be
 *    swapped in without touching any caller.
 */
public interface IProductService {

    /**
     * Return a paginated list of all products from MySQL.
     *
     * @param page zero-based page index
     * @param size number of items per page (max 100)
     */
    ApiListResponse<ProductResponse> listProducts(int page, int size);

    /**
     * Filter products by category name or slug from MySQL.
     *
     * @param category category name or URL slug (e.g. "smartphones")
     * @param page     zero-based page index
     * @param size     page size
     */
    ApiListResponse<ProductResponse> filterByCategory(String category, int page, int size);

    /**
     * Fetch a single product by its ID from MySQL.
     * Throws ResourceNotFoundException if not found.
     *
     * @param id product identifier
     */
    ProductResponse getById(Integer id);

    /**
     * Full-text search via Elasticsearch.
     * Optionally filtered by category.
     *
     * @param query    search text
     * @param category optional category filter (may be null)
     * @param page     zero-based page index
     * @param size     page size
     */
    List<ProductResponse> searchProducts(String query, String category, int page, int size);
}
