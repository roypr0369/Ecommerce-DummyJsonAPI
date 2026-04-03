package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a product category.
 * Normalised into its own table to avoid repeating category strings across every product row.
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    /** URL-friendly version of the name (e.g. "beauty" → "beauty", "mens-shirts" → "mens-shirts") */
    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();

    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }
}
