package com.ecommerce.controller;

import com.ecommerce.dto.ApiListResponse;
import com.ecommerce.dto.CategoryResponse;
import com.ecommerce.service.ICategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for category endpoints.
 * Endpoint:
 *   GET /categories → list all categories (MySQL)
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<ApiListResponse<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.listAll());
    }
}
