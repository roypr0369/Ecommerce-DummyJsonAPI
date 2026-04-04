package com.ecommerce.service.impl;

import com.ecommerce.dto.ApiListResponse;
import com.ecommerce.dto.CategoryResponse;
import com.ecommerce.model.Category;
import com.ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void testListAll_success() {
        // Arrange
        Category c1 = new Category();
        c1.setId(1L);
        c1.setName("Electronics");
        c1.setSlug("electronics");

        Category c2 = new Category();
        c2.setId(2L);
        c2.setName("Books");
        c2.setSlug("books");

        List<Category> mockCategories = List.of(c1, c2);

        when(categoryRepository.findAll(Sort.by("name").ascending()))
                .thenReturn(mockCategories);

        // Act
        ApiListResponse<CategoryResponse> response = categoryService.listAll();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getTotal());
        assertEquals(2, response.getData().size());

        // Validate mapping
        CategoryResponse res1 = response.getData().get(0);
        assertEquals("Electronics", res1.getName());
        assertEquals("electronics", res1.getSlug());

        CategoryResponse res2 = response.getData().get(1);
        assertEquals("Books", res2.getName());
        assertEquals("books", res2.getSlug());

        // Verify repository interaction
        verify(categoryRepository, times(1))
                .findAll(Sort.by("name").ascending());
    }

    @Test
    void testListAll_emptyList() {
        // Arrange
        when(categoryRepository.findAll(Sort.by("name").ascending()))
                .thenReturn(List.of());

        // Act
        ApiListResponse<CategoryResponse> response = categoryService.listAll();

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getTotal());
        assertTrue(response.getData().isEmpty());

        verify(categoryRepository, times(1))
                .findAll(Sort.by("name").ascending());
    }

    @Test
    void testListAll_singleCategory() {
        // Arrange
        Category c1 = new Category();
        c1.setId(1L);
        c1.setName("Fashion");
        c1.setSlug("fashion");

        when(categoryRepository.findAll(Sort.by("name").ascending()))
                .thenReturn(List.of(c1));

        // Act
        ApiListResponse<CategoryResponse> response = categoryService.listAll();

        // Assert
        assertEquals(1, response.getTotal());
        assertEquals("Fashion", response.getData().get(0).getName());
    }
}