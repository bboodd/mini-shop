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

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;
	private final RecentViewService recentViewService;

	@GetMapping
	public ResponseEntity<List<Product>> getAllProducts() {
		return ResponseEntity.ok(productService.getAllProducts());
	}

	@GetMapping("/{id}")
	public ResponseEntity<Product> getProduct(
		@PathVariable Long id,
		@RequestParam(required = false) String userId
	) {
		Product product = productService.getProduct(id);

		// userId가 제공된 경우 최근 본 상품에 추가
		if (userId != null && !userId.isEmpty()) {
			recentViewService.addRecentView(userId, id);
		}

		return ResponseEntity.ok(product);
	}

	@PostMapping
	public ResponseEntity<Product> createProduct(@RequestBody Product product) {
		return ResponseEntity.status(HttpStatus.CREATED)
			.body(productService.createProduct(product));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Product> updateProduct(
		@PathVariable Long id,
		@RequestBody Product product
	) {
		return ResponseEntity.ok(productService.updateProduct(id, product));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
		productService.deleteProduct(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/search")
	public ResponseEntity<List<ProductDocument>> searchProducts(
		@RequestParam String keyword
	) {
		return ResponseEntity.ok(productService.searchProducts(keyword));
	}

	@GetMapping("/category/{category}")
	public ResponseEntity<List<Product>> getProductByCategory(
		@PathVariable String category
	) {
		return ResponseEntity.ok(productService.getProductByCategory(category));
	}
}
