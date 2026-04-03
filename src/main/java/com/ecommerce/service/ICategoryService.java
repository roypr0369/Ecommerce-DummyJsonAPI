package com.ecommerce.service;

import com.ecommerce.dto.ApiListResponse;
import com.ecommerce.dto.CategoryResponse;


public interface ICategoryService {
    ApiListResponse<CategoryResponse> listAll();
}
