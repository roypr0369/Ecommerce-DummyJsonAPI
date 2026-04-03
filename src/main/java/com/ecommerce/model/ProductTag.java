package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One tag per row — same normalisation rationale as ProductImage.
 */
@Entity
@Table(
    name = "product_tags",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_product_tag",
        columnNames = {"product_id", "tag"}
    )
)
@NoArgsConstructor
@Data
public class ProductTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String tag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    public ProductTag(String tag, Product product) {
        this.tag = tag;
        this.product = product;
    }
}
