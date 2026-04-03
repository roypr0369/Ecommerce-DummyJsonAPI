package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * DTO for a single product returned by https://dummyjson.com/products.
 *
 * @JsonIgnoreProperties(ignoreUnknown = true) means we silently drop any
 * fields from the API that we haven't mapped — keeps the code forward-compatible.
 */

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
