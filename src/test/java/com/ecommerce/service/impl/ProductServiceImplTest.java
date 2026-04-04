package com.ecommerce.service.impl;

import com.ecommerce.document.ProductDocument;
import com.ecommerce.dto.ApiListResponse;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.ProductMapper;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    // ❌ NOT mocking mapper anymore
    private ProductMapper productMapper;

    private ProductServiceImpl productService;

    @BeforeEach
    void setup() {
        // ✅ Use real mapper
        productMapper = new ProductMapper(); // OR ProductMapperImpl if MapStruct

        // ✅ Manual injection
        productService = new ProductServiceImpl(
                productRepository,
                elasticsearchOperations,
                productMapper
        );
    }

    // ─────────────────────────────────────────────────────────
    // ✅ listProducts
    // ─────────────────────────────────────────────────────────
    @Test
    void testListProducts_success() {
        Product p = new Product();
        p.setId(1);

        Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);

        ApiListResponse<ProductResponse> response =
                productService.listProducts(0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals(1, response.getData().size());

        verify(productRepository).findAll(any(Pageable.class));
    }

    // ─────────────────────────────────────────────────────────
    // ✅ filterByCategory
    // ─────────────────────────────────────────────────────────
    @Test
    void testFilterByCategory_success() {
        Product p = new Product();
        p.setId(1);

        Page<Product> page = new PageImpl<>(List.of(p), PageRequest.of(0, 10), 1);

        when(productRepository.findByCategoryNameOrSlug(eq("electronics"), any(Pageable.class)))
                .thenReturn(page);

        ApiListResponse<ProductResponse> response =
                productService.filterByCategory("electronics", 0, 10);

        assertEquals(1, response.getTotal());
        assertEquals(1, response.getData().size());

        verify(productRepository)
                .findByCategoryNameOrSlug(eq("electronics"), any(Pageable.class));
    }

    // ─────────────────────────────────────────────────────────
    // ✅ getById (success)
    // ─────────────────────────────────────────────────────────
    @Test
    void testGetById_success() {
        Product product = new Product();
        product.setId(1);

        when(productRepository.findById(1)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getById(1);

        assertNotNull(response);
        verify(productRepository).findById(1);
    }

    // ─────────────────────────────────────────────────────────
    // ❌ getById (not found)
    // ─────────────────────────────────────────────────────────
    @Test
    void testGetById_notFound() {
        when(productRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getById(1));
    }
}