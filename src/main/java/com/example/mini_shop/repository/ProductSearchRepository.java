package com.example.mini_shop.repository;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.example.mini_shop.document.ProductDocument;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {
	List<ProductDocument> findByNameContaining(String name);
	List<ProductDocument> findByCategory(String category);
	List<ProductDocument> findByNameContainingOrDescriptionContaining(String name, String description);
}
