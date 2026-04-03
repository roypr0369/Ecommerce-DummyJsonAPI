package com.ecommerce.repository;

import com.ecommerce.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Basic CRUD operations on the Elasticsearch index.
 */
@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Integer> {
}
