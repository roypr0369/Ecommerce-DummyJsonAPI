package com.ecommerce.service.impl;

import com.ecommerce.dto.ApiListResponse;
import com.ecommerce.dto.CategoryResponse;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.service.ICategoryService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements ICategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ApiListResponse<CategoryResponse> listAll() {
        List<CategoryResponse> categories = categoryRepository
                .findAll(Sort.by("name").ascending())
                .stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName(), c.getSlug()))
                .collect(Collectors.toList());

        return new ApiListResponse<>(categories.size(), 0, categories.size(), categories);
    }
}
