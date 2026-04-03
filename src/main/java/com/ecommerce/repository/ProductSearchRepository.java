package com.ecommerce.repository;

import com.ecommerce.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Basic CRUD operations on the Elasticsearch index.
 * Full-text search is handled in ProductService via ElasticsearchOperations
 * because we need fine-grained control over the query DSL (multi-match + filter + fuzzy).
 */
@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Integer> {
}
