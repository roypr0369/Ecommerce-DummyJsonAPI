package com.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Generic paginated response wrapper.
 * Using a generic here means one class serves both
 * ApiListResponse<ProductResponse> and ApiListResponse<CategoryResponse>.
 */

@Data
@AllArgsConstructor
public class ApiListResponse<T> {
    private long total;
    private int page;
    private int size;
    private List<T> data;
}
