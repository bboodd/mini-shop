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

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

	private final CartService cartService;

	@PostMapping
	public ResponseEntity<Void> addToCart(@RequestBody CartRequest request) {
		cartService.addToCart(request.getUserId(), request.getProductId(), request.getQuantity());
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{userId}")
	public ResponseEntity<List<CartItem>> getCart(@PathVariable String userId) {
		return ResponseEntity.ok(cartService.getCart(userId));
	}

	@PutMapping
	public ResponseEntity<Void> updateCartItem(@RequestBody CartRequest request) {
		cartService.updateCartItem(request.getUserId(), request.getProductId(), request.getQuantity());
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{userId}/{productId}")
	public ResponseEntity<Void> removeFromCart(
		@PathVariable String userId,
		@PathVariable Long productId
	) {
		cartService.removeFromCart(userId, productId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> clearCart(@PathVariable String userId) {
		cartService.clearCart(userId);
		return ResponseEntity.noContent().build();
	}
}
