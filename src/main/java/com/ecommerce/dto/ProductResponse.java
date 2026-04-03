package com.ecommerce.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductResponse {
    private Integer id;
    private String title;
    private String description;
    private Double price;
    private Double discountPercentage;
    private Double rating;
    private Integer stock;
    private String brand;
    private String thumbnail;
    private String category;
    private List<String> images;
    private List<String> tags;
    private LocalDateTime createdAt;
    private String sku;
    private Double weight;
    private Double dimWidth;
    private Double dimHeight;
    private Double dimDepth;
    private String warrantyInformation;
    private String shippingInformation;
    private String availabilityStatus;
    private String returnPolicy;
    private Integer minimumOrderQuantity;
    private String barcode;
    private List<ReviewResponse> reviews = new ArrayList<>();
}
