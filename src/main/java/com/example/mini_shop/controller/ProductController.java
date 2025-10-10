package com.example.mini_shop.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mini_shop.document.ProductDocument;
import com.example.mini_shop.entity.Product;
import com.example.mini_shop.service.ProductService;
import com.example.mini_shop.service.RecentViewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;
	private final RecentViewService recentViewService;

	@GetMapping
	public ResponseEntity<List<Product>> getAllProducts() {
		log.info("API Request: GET /api/products - Get all products");
		List<Product> products = productService.getAllProducts();
		log.info("API Response: Returning {} products", products.size());
		return ResponseEntity.ok(products);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Product> getProduct(
		@PathVariable Long id,
		@RequestParam(required = false) String userId) {
		log.info("API Request: GET /api/products/{} - userId: {}", id, userId);

		Product product = productService.getProduct(id);

		// userId가 제공된 경우 최근 본 상품에 추가
		if (userId != null && !userId.isEmpty()) {
			log.debug("Adding product {} to recent views for user: {}", id, userId);
			recentViewService.addRecentView(userId, id);
		}

		log.info("API Response: Product {} retrieved successfully", id);
		return ResponseEntity.ok(product);
	}

	@PostMapping
	public ResponseEntity<Product> createProduct(@RequestBody Product product) {
		log.info("API Request: POST /api/products - Creating product: {}", product.getName());
		Product createdProduct = productService.createProduct(product);
		log.info("API Response: Product created with id: {}", createdProduct.getId());
		return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Product> updateProduct(
		@PathVariable Long id,
		@RequestBody Product product) {
		log.info("API Request: PUT /api/products/{} - Updating product", id);
		Product updatedProduct = productService.updateProduct(id, product);
		log.info("API Response: Product {} updated successfully", id);
		return ResponseEntity.ok(updatedProduct);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
		log.info("API Request: DELETE /api/products/{} - Deleting product", id);
		productService.deleteProduct(id);
		log.info("API Response: Product {} deleted successfully", id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/search")
	public ResponseEntity<List<ProductDocument>> searchProducts(
		@RequestParam String keyword) {
		log.info("API Request: GET /api/products/search - keyword: {}", keyword);
		List<ProductDocument> results = productService.searchProducts(keyword);
		log.info("API Response: Found {} products for keyword: {}", results.size(), keyword);
		return ResponseEntity.ok(results);
	}

	@GetMapping("/category/{category}")
	public ResponseEntity<List<Product>> getProductsByCategory(
		@PathVariable String category) {
		log.info("API Request: GET /api/products/category/{}", category);
		List<Product> products = productService.getProductsByCategory(category);
		log.info("API Response: Found {} products in category: {}", products.size(), category);
		return ResponseEntity.ok(products);
	}
}
