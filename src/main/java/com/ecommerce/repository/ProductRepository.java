package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    /**
     * Filter products by category name OR slug.
     * Supports both "smartphones" and "smartphones" (slug == name here)
     * as well as human-readable names from the API.
     */
    @Query("SELECT p FROM Product p WHERE p.category.name = :value OR p.category.slug = :value")
    Page<Product> findByCategoryNameOrSlug(@Param("value") String value, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.name = :value OR p.category.slug = :value")
    long countByCategoryNameOrSlug(@Param("value") String value);
}
