package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One image URL per row — avoids storing JSON arrays in a column,
 * making each URL individually queryable and indexable.
 */
@Entity
@Table(
    name = "product_images",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_product_image",
        columnNames = {"product_id", "url"}
    )
)
@Data
@NoArgsConstructor
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public ProductImage(String url, Product product) {
        this.url = url;
        this.product = product;
    }
}
