package com.example.mini_shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.mini_shop.entity.Product;
import com.example.mini_shop.model.CartItem;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ProductService productService;
	private final ObjectMapper objectMapper;
	private static final String CART_KEY_PREFIX = "cart:";
	private static final long CART_TTL = 24; // 24 hour

	public void addToCart(String userId, Long productId, Integer quantity) {
		Product product = productService.getProduct(productId);

		if (product.getStock() < quantity) {
			throw new RuntimeException("Insufficient stock");
		}

		String key = CART_KEY_PREFIX + userId;
		CartItem cartItem = CartItem.builder()
			.productId(productId)
			.productName(product.getName())
			.price(product.getPrice())
			.quantity(quantity)
			.build();

		redisTemplate.opsForHash().put(key, String.valueOf(productId), cartItem);
		redisTemplate.expire(key, CART_TTL, TimeUnit.HOURS);
	}

	public List<CartItem> getCart(String userId) {
		String key = CART_KEY_PREFIX + userId;
		List<Object> items = redisTemplate.opsForHash().values(key);

		List<CartItem> cartItems = new ArrayList<>();
		for (Object item : items) {
			CartItem cartItem = objectMapper.convertValue(item, CartItem.class);
			cartItems.add(cartItem);
			// LinkedHashMap 에러
			// cartItems.add((CartItem) item);
		}
		return cartItems;
	}

	public void updateCartItem(String userId, Long productId, Integer quantity) {
		String key = CART_KEY_PREFIX + userId;
		CartItem cartItem = (CartItem) redisTemplate.opsForHash().get(key, String.valueOf(productId));

		if (cartItem == null) {
			throw new RuntimeException("Cart item not found");
		}

		Product product = productService.getProduct(productId);
		if (product.getStock() < quantity) {
			throw new RuntimeException("Insufficient stock");
		}

		cartItem.setQuantity(quantity);
		redisTemplate.opsForHash().put(key, String.valueOf(productId), cartItem);
	}

	public void removeFromCart(String userId, Long productId) {
		String key = CART_KEY_PREFIX + userId;
		redisTemplate.opsForHash().delete(key, String.valueOf(productId));
	}

	public void clearCart(String userId) {
		String key = CART_KEY_PREFIX + userId;
		redisTemplate.delete(key);
	}
}
