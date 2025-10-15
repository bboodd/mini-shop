package com.example.mini_shop.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mini_shop.document.ProductDocument;
import com.example.mini_shop.entity.Product;
import com.example.mini_shop.event.ElasticsearchIndexEvent;
import com.example.mini_shop.event.StockUpdatedEvent;
import com.example.mini_shop.messaging.EventPublisher;
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
	private final EventPublisher eventPublisher;


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

		// Elasticsearch 인덱싱 이벤트 발행 (비동기)
		eventPublisher.publishElasticsearchIndex(
			ElasticsearchIndexEvent.builder()
				.productId(savedProduct.getId())
				.operation("INDEX")
				.timestamp(LocalDateTime.now())
				.build()
		);
		return savedProduct;
	}

	@Transactional
	@CacheEvict(value = "products", allEntries = true)
	public Product updateProduct(Long id, Product productDetails) {
		log.info("Updating product with id: {}", id);
		Product product = getProduct(id);

		Integer previousStock = product.getStock();

		product.setName(productDetails.getName());
		product.setDescription(productDetails.getDescription());
		product.setPrice(productDetails.getPrice());
		product.setStock(productDetails.getStock());
		product.setCategory(productDetails.getCategory());

		Product updatedProduct = productRepository.save(product);
		log.info("Product updated successfully: {}", id);

		// 재고 변동이 있으면 재고 업데이트 이벤트 발행
		if (!previousStock.equals(productDetails.getStock())) {
			eventPublisher.publishStockUpdated(
				StockUpdatedEvent.builder()
					.productId(updatedProduct.getId())
					.productName(updatedProduct.getName())
					.previousStock(previousStock)
					.currentStock(updatedProduct.getStock())
					.operation(updatedProduct.getStock() > previousStock ? "INCREASE" : "DECREASE")
					.updatedAt(LocalDateTime.now())
					.build()
			);
		}

		// Elasticsearch 업데이트 이벤트 발행 (비동기)
		eventPublisher.publishElasticsearchIndex(
			ElasticsearchIndexEvent.builder()
				.productId(updatedProduct.getId())
				.operation("UPDATE")
				.timestamp(LocalDateTime.now())
				.build()
		);
		return updatedProduct;
	}

	@Transactional
	@CacheEvict(value = "products", allEntries = true)
	public void deleteProduct(Long id) {
		log.info("Deleting product with id: {}", id);
		productRepository.deleteById(id);

		// Elasticsearch 삭제 이벤트 발행 (비동기)
		eventPublisher.publishElasticsearchIndex(
			ElasticsearchIndexEvent.builder()
				.productId(id)
				.operation("DELETE")
				.timestamp(LocalDateTime.now())
				.build()
		);
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

	@Transactional
	public void decreaseStock(Long productId, Integer quantity) {
		log.info("Decreasing stock for product: {}, quantity: {}", productId, quantity);
		Product product = getProduct(productId);

		if (product.getStock() < quantity) {
			log.error("Insufficient stock: productId={}, requested={}, available={}",
				productId, quantity, product.getStock());
			throw new RuntimeException("Insufficient stock");
		}

		Integer previousStock = product.getStock();
		product.setStock(product.getStock() - quantity);
		productRepository.save(product);

		log.info("Stock decreased successfully: productId={}, previousStock={}, currentStock={}",
			productId, previousStock, product.getStock());

		// 재고 업데이트 이벤트 발행
		eventPublisher.publishStockUpdated(
			StockUpdatedEvent.builder()
				.productId(product.getId())
				.productName(product.getName())
				.previousStock(previousStock)
				.currentStock(product.getStock())
				.operation("DECREASE")
				.updatedAt(LocalDateTime.now())
				.build()
		);
	}
}
