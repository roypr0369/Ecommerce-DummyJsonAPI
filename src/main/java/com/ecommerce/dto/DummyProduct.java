package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DummyProduct {

    private Integer id;
    private String title;
    private String description;
    private Double price;

    @JsonProperty("discountPercentage")
    private Double discountPercentage;

    private Double rating;
    private Integer stock;
    private String brand;
    private String category;
    private String thumbnail;
    private List<String> images;
    private List<String> tags;
    private String sku;
    private Double weight;
    private DummyDimensions dimensions;
    private String warrantyInformation;
    private String shippingInformation;
    private String availabilityStatus;
    private String returnPolicy;
    private Integer minimumOrderQuantity;
    private List<DummyReview> reviews;
    private DummyMeta meta;
}
