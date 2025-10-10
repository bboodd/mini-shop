package com.example.mini_shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.mini_shop.entity.Product;
import com.example.mini_shop.model.CartItem;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ProductService productService;
	private final ObjectMapper objectMapper;
	private static final String CART_KEY_PREFIX = "cart:";
	private static final long CART_TTL = 24; // 24 hour

	public void addToCart(String userId, Long productId, Integer quantity) {
		log.info("Adding product {} to cart for user: {}, quantity: {}", productId, userId, quantity);

		Product product = productService.getProduct(productId);

		if (product.getStock() < quantity) {
			log.warn("Insufficient stock for product {}: requested={}, available={}",
				productId, quantity, product.getStock());
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

		log.info("Product {} added to cart successfully for user: {}", productId, userId);
	}

	public List<CartItem> getCart(String userId) {
		log.debug("Fetching cart for user: {}", userId);
		String key = CART_KEY_PREFIX + userId;
		List<Object> items = redisTemplate.opsForHash().values(key);

		List<CartItem> cartItems = new ArrayList<>();
		for (Object item : items) {
			try {
				if (item instanceof CartItem) {
					cartItems.add((CartItem) item);
				} else if (item instanceof Map) {
					CartItem cartItem = objectMapper.convertValue(item, CartItem.class);
					cartItems.add(cartItem);
				}
			} catch (Exception e) {
				log.error("Failed to convert cart item: {}", e.getMessage());
			}
		}

		log.debug("Found {} items in cart for user: {}", cartItems.size(), userId);
		return cartItems;
	}

	public void updateCartItem(String userId, Long productId, Integer quantity) {
		log.info("Updating cart item for user: {}, product: {}, new quantity: {}",
			userId, productId, quantity);

		String key = CART_KEY_PREFIX + userId;
		Object obj = redisTemplate.opsForHash().get(key, String.valueOf(productId));

		if (obj == null) {
			log.error("Cart item not found for user: {}, product: {}", userId, productId);
			throw new RuntimeException("Cart item not found");
		}

		CartItem cartItem;
		try {
			if (obj instanceof CartItem) {
				cartItem = (CartItem) obj;
			} else if (obj instanceof Map) {
				cartItem = objectMapper.convertValue(obj, CartItem.class);
			} else {
				throw new RuntimeException("Invalid cart item type");
			}
		} catch (Exception e) {
			log.error("Failed to get cart item: {}", e.getMessage());
			throw new RuntimeException("Failed to get cart item: " + e.getMessage());
		}

		Product product = productService.getProduct(productId);
		if (product.getStock() < quantity) {
			log.warn("Insufficient stock for product {}: requested={}, available={}",
				productId, quantity, product.getStock());
			throw new RuntimeException("Insufficient stock");
		}

		cartItem.setQuantity(quantity);
		redisTemplate.opsForHash().put(key, String.valueOf(productId), cartItem);

		log.info("Cart item updated successfully for user: {}, product: {}", userId, productId);
	}

	public void removeFromCart(String userId, Long productId) {
		log.info("Removing product {} from cart for user: {}", productId, userId);
		String key = CART_KEY_PREFIX + userId;
		redisTemplate.opsForHash().delete(key, String.valueOf(productId));
		log.info("Product {} removed from cart for user: {}", productId, userId);
	}

	public void clearCart(String userId) {
		log.info("Clearing cart for user: {}", userId);
		String key = CART_KEY_PREFIX + userId;
		redisTemplate.delete(key);
		log.info("Cart cleared for user: {}", userId);
	}
}
