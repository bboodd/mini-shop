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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductSearchRepository productSearchRepository;

	// 개별 상품 조회는 캐시하지 않음 (LinkedHashMap 문제 방지)
	public Product getProduct(Long id) {
		log.debug("Fetching product with id: {}", id);
		return productRepository.findById(id)
			.orElseThrow(() -> {
				log.error("Product not found with id: {}", id);
				return new RuntimeException("Product not found");
			});
	}

	@Cacheable(value = "products", key = "'all'")
	public List<Product> getAllProducts() {
		log.info("Fetching all products");
		List<Product> products = productRepository.findAll();
		log.info("Found {} products", products.size());
		return products;
	}

	@Transactional
	@CacheEvict(value = "products", allEntries = true)
	public Product createProduct(Product product) {
		log.info("Creating new product: {}", product.getName());
		Product savedProduct = productRepository.save(product);
		// Elasticsearch에 인덱싱
		productSearchRepository.save(ProductDocument.from(savedProduct));
		log.info("Product created successfully with id: {}", savedProduct.getId());
		return savedProduct;
	}

	@Transactional
	@CacheEvict(value = "products", allEntries = true)
	public Product updateProduct(Long id, Product productDetails) {
		log.info("Updating product with id: {}", id);
		Product product = getProduct(id);
		product.setName(productDetails.getName());
		product.setDescription(productDetails.getDescription());
		product.setPrice(productDetails.getPrice());
		product.setStock(productDetails.getStock());
		product.setCategory(productDetails.getCategory());

		Product updatedProduct = productRepository.save(product);
		// Elasticsearch 업데이트
		productSearchRepository.save(ProductDocument.from(updatedProduct));
		log.info("Product updated successfully: {}", id);
		return updatedProduct;
	}

	@Transactional
	@CacheEvict(value = "products", allEntries = true)
	public void deleteProduct(Long id) {
		log.info("Deleting product with id: {}", id);
		productRepository.deleteById(id);
		productSearchRepository.deleteById(String.valueOf(id));
		log.info("Product deleted successfully: {}", id);
	}

	public List<ProductDocument> searchProducts(String keyword) {
		log.info("Searching products with keyword: {}", keyword);
		List<ProductDocument> results = productSearchRepository
			.findByNameContainingOrDescriptionContaining(keyword, keyword);
		log.info("Found {} products matching keyword: {}", results.size(), keyword);
		return results;
	}

	public List<Product> getProductsByCategory(String category) {
		log.info("Fetching products by category: {}", category);
		List<Product> products = productRepository.findByCategory(category);
		log.info("Found {} products in category: {}", products.size(), category);
		return products;
	}
}
