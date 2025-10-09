package com.example.mini_shop.service;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mini_shop.document.ProductDocument;
import com.example.mini_shop.entity.Product;
import com.example.mini_shop.repository.ProductRepository;
import com.example.mini_shop.repository.ProductSearchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductSearchRepository productSearchRepository;

	// 개별 상품 조회는 캐시하지 않음 (LinkedHashMap 문제 방지)
	public Product getProduct(Long id) {
		return productRepository.findById(id)
			.orElseThrow(() -> new RuntimeException("Product not found"));
	}

	@Cacheable(value = "products", key = "'all'")
	public List<Product> getAllProducts() {
		return productRepository.findAll();
	}

	@Transactional
	@CacheEvict(value = "products", allEntries = true)
	public Product createProduct(Product product) {
		Product savedProduct = productRepository.save(product);
		// Elasticsearch
		productSearchRepository.save(ProductDocument.from(savedProduct));
		return savedProduct;
	}

	@Transactional
	@CacheEvict(value = "products", allEntries = true)
	public Product updateProduct(Long id, Product productDetails) {
		Product product = getProduct(id);
		product.setName(productDetails.getName());
		product.setDescription(productDetails.getDescription());
		product.setPrice(productDetails.getPrice());
		product.setStock(productDetails.getStock());
		product.setCategory(productDetails.getCategory());

		Product updatedProduct = productRepository.save(product);
		// Elasticsearch
		productSearchRepository.save(ProductDocument.from(updatedProduct));
		return updatedProduct;
	}

	@Transactional
	@CacheEvict(value = "products", allEntries = true)
	public void deleteProduct(Long id) {
		productRepository.deleteById(id);
		productSearchRepository.deleteById(String.valueOf(id));
	}

	public List<ProductDocument> searchProducts(String keyword) {
		return productSearchRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
	}

	public List<Product> getProductByCategory(String category) {
		return productRepository.findByCategory(category);
	}
}
