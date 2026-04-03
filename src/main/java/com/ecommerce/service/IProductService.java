package com.ecommerce.service;

import com.ecommerce.dto.ApiListResponse;
import com.ecommerce.dto.ProductResponse;

import java.util.List;

public interface IProductService {
    ApiListResponse<ProductResponse> listProducts(int page, int size);
    ApiListResponse<ProductResponse> filterByCategory(String category, int page, int size);
    ProductResponse getById(Integer id);
    List<ProductResponse> searchProducts(String query, String category, int page, int size);
}
