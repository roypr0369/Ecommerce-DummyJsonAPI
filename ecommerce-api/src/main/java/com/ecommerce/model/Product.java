package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core product entity — maps to the `products` MySQL table.
 *
 * We use the dummyjson product id directly as the primary key so there
 * is no extra indirection layer between the external data source and our DB.
 */
@Entity
@Table(name = "products")
@Data
public class Product {

    /** Use dummyjson's own id as PK — avoids a separate mapping table. */
    @Id
    private Integer id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(precision = 3, scale = 2)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 120)
    private String brand;

    @Column(columnDefinition = "TEXT")
    private String thumbnail;

    @Column(length = 100)
    private String sku;

    @Column
    private Double weight;

    // Dimensions stored as 3 flat columns — simpler than a separate table
    @Column(name = "dim_width")
    private Double dimWidth;

    @Column(name = "dim_height")
    private Double dimHeight;

    @Column(name = "dim_depth")
    private Double dimDepth;

    @Column(name = "warranty_information", length = 255)
    private String warrantyInformation;

    @Column(name = "shipping_information", length = 255)
    private String shippingInformation;

    @Column(name = "availability_status", length = 50)
    private String availabilityStatus;

    @Column(name = "return_policy", length = 255)
    private String returnPolicy;

    @Column(name = "minimum_order_quantity")
    private Integer minimumOrderQuantity;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // ── Relations ─────────────────────────────────────────────────────────────

    /** FK to the normalised categories table. */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Images stored in a child table (1-to-many) so they can be individually
     * indexed / queried. CascadeType.ALL + orphanRemoval keeps them in sync.
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductImage> images = new ArrayList<>();

    /** Tags stored in a child table for the same reasons as images. */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductTag> tags = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProductReview> reviews = new ArrayList<>();

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ── Convenience helpers ───────────────────────────────────────────────────

    public void addImage(String url) {
        ProductImage img = new ProductImage(url, this);
        this.images.add(img);
    }

    public void addTag(String tag) {
        ProductTag t = new ProductTag(tag, this);
        this.tags.add(t);
    }
}
