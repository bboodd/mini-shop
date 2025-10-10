package com.example.mini_shop.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mini_shop.dto.CartRequest;
import com.example.mini_shop.model.CartItem;
import com.example.mini_shop.service.CartService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

	private final CartService cartService;

	@PostMapping
	public ResponseEntity<Void> addToCart(@RequestBody CartRequest request) {
		log.info("API Request: POST /api/cart - userId: {}, productId: {}, quantity: {}",
			request.getUserId(), request.getProductId(), request.getQuantity());

		try {
			cartService.addToCart(request.getUserId(), request.getProductId(), request.getQuantity());
			log.info("API Response: Item added to cart successfully");
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error("API Error: Failed to add item to cart - {}", e.getMessage());
			throw e;
		}
	}

	@GetMapping("/{userId}")
	public ResponseEntity<List<CartItem>> getCart(@PathVariable String userId) {
		log.info("API Request: GET /api/cart/{}", userId);
		List<CartItem> cartItems = cartService.getCart(userId);
		log.info("API Response: Returning {} items in cart for user: {}", cartItems.size(), userId);
		return ResponseEntity.ok(cartItems);
	}

	@PutMapping
	public ResponseEntity<Void> updateCartItem(@RequestBody CartRequest request) {
		log.info("API Request: PUT /api/cart - userId: {}, productId: {}, newQuantity: {}",
			request.getUserId(), request.getProductId(), request.getQuantity());

		try {
			cartService.updateCartItem(request.getUserId(), request.getProductId(), request.getQuantity());
			log.info("API Response: Cart item updated successfully");
			return ResponseEntity.ok().build();
		} catch (Exception e) {
			log.error("API Error: Failed to update cart item - {}", e.getMessage());
			throw e;
		}
	}

	@DeleteMapping("/{userId}/{productId}")
	public ResponseEntity<Void> removeFromCart(
		@PathVariable String userId,
		@PathVariable Long productId) {
		log.info("API Request: DELETE /api/cart/{}/{} - Removing item from cart", userId, productId);
		cartService.removeFromCart(userId, productId);
		log.info("API Response: Item removed from cart successfully");
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> clearCart(@PathVariable String userId) {
		log.info("API Request: DELETE /api/cart/{} - Clearing cart", userId);
		cartService.clearCart(userId);
		log.info("API Response: Cart cleared successfully for user: {}", userId);
		return ResponseEntity.noContent().build();
	}
}
