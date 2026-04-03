package com.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_reviews")
@Data
@NoArgsConstructor
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "review_date", length = 50)
    private String reviewDate;

    @Column(name = "reviewer_name", length = 120)
    private String reviewerName;

    @Column(name = "reviewer_email", length = 120)
    private String reviewerEmail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;


    public ProductReview(Integer rating, String comment, String reviewDate,
                         String reviewerName, String reviewerEmail, Product product) {
        this.rating = rating;
        this.comment = comment;
        this.reviewDate = reviewDate;
        this.reviewerName = reviewerName;
        this.reviewerEmail = reviewerEmail;
        this.product = product;
    }
}
