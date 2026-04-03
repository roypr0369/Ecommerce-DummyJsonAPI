package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/** Top-level response from https://dummyjson.com/products */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DummyJsonResponse {
    private List<DummyProduct> products;
    private int total;
    private int skip;
    private int limit;
}
