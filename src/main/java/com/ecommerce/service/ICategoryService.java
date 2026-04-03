package com.ecommerce.service;

import com.ecommerce.dto.ApiListResponse;
import com.ecommerce.dto.CategoryResponse;

/**
 * Contract for category-related operations.
 *
 * Design Principles:
 *  - ISP: completely separate from IProductService — no unnecessary methods.
 *  - DIP: CategoryController depends on this interface, not the concrete class.
 */
public interface ICategoryService {

    /**
     * Return all categories ordered alphabetically.
     */
    ApiListResponse<CategoryResponse> listAll();
}
