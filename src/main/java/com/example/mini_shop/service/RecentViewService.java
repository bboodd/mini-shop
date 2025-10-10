package com.example.mini_shop.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.mini_shop.entity.Product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecentViewService {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ProductService productService;

	private static final String RECENT_VIEW_KEY_PREFIX = "recent_view:";
	private static final int MAX_RECENT_ITEMS = 10; // 최대 10개 저장
	private static final long TTL_DAYS = 30; // 30일 보관

	/**
	 * 최근 본 상품 추가
	 * ZSet 사용 timestamp를 score로 저장
	 */
	public void addRecentView(String userId, Long productId) {
		log.info("Adding product {} to recent views for user: {}", productId, userId);

		String key = RECENT_VIEW_KEY_PREFIX + userId;
		double score = System.currentTimeMillis();

		// ZSet에 추가
		redisTemplate.opsForZSet().add(key, productId.toString(), score);

		// 최대 개수 초과 시 오래된 항목 삭제
		Long size = redisTemplate.opsForZSet().size(key);
		if (size != null && size > MAX_RECENT_ITEMS) {
			Long removed = redisTemplate.opsForZSet().removeRange(key, 0, size - MAX_RECENT_ITEMS - 1);
			log.debug("Removed {} old items from recent views for user: {}", removed, userId);
		}

		// TTL 설정
		redisTemplate.expire(key, TTL_DAYS, TimeUnit.DAYS);

		log.debug("Recent view added successfully. Current size: {} for user: {}", size, userId);
	}

	/**
	 * 최근 본 상품 목록 조회 ( 최신순 )
	 */
	public List<Product> getRecentViews(String userId) {
		log.info("Fetching recent views for user: {}", userId);

		String key = RECENT_VIEW_KEY_PREFIX + userId;

		// ZSet에서 최신순으로 조회 ( 역순 )
		Set<Object> productIds = redisTemplate.opsForZSet()
			.reverseRange(key, 0, MAX_RECENT_ITEMS - 1);

		if (productIds == null || productIds.isEmpty()) {
			log.info("No recent views found for user: {}", userId);
			return new ArrayList<>();
		}

		log.debug("Found {} product IDs in recent views for user: {}", productIds.size(), userId);

		// Products information
		List<Product> recentProducts = new ArrayList<>();
		for (Object obj : productIds) {
			try {
				Long productId = Long.parseLong(obj.toString());
				Product product = productService.getProduct(productId);
				recentProducts.add(product);
				log.debug("Added product {} to recent views list", productId);
			} catch (Exception e) {
				log.warn("Failed to load product from recent views: {}", e.getMessage());
			}
		}

		log.info("Returning {} recent products for user: {}", recentProducts.size(), userId);

		return recentProducts;
	}

	/**
	 * 최근 본 상품 개수 조회
	 */
	public Long getRecentViewCount(String userId) {
		log.debug("Getting recent view count for user: {}", userId);

		String key = RECENT_VIEW_KEY_PREFIX + userId;
		Long count = redisTemplate.opsForZSet().size(key);

		log.info("Recent view count for user {}: {}", userId, count != null ? count : 0);
		return count != null ? count : 0L;
	}

	/**
	 * 특정 상품 제거
	 */
	public void removeRecentView(String userId, Long productId) {
		log.info("Removing product {} from recent views for user: {}", productId, userId);

		String key = RECENT_VIEW_KEY_PREFIX + userId;
		Long removed = redisTemplate.opsForZSet().remove(key, productId.toString());

		if (removed != null && removed > 0) {
			log.info("Product {} removed successfully from recent views for user: {}", productId, userId);
		} else {
			log.warn("Product {} was not in recent views for user: {}", productId, userId);
		}
	}

	/**
	 * 전체 최근 본 상품 삭제
	 */
	public void clearRecentViews(String userId) {
		log.info("Clearing all recent views for user: {}", userId);

		String key = RECENT_VIEW_KEY_PREFIX + userId;
		Boolean deleted = redisTemplate.delete(key);

		if (deleted) {
			log.info("Recent views cleared successfully for user: {}", userId);
		} else {
			log.warn("No recent views found to clear for user: {}", userId);
		}
	}

	/**
	 * 최근 본 상품 ID 목록만 조회
	 */
	public List<Long> getRecentViewIds(String userId) {
		log.debug("Fetching recent view IDs for user: {}", userId);

		String key = RECENT_VIEW_KEY_PREFIX + userId;
		Set<Object> productIds = redisTemplate.opsForZSet()
			.reverseRange(key, 0, MAX_RECENT_ITEMS - 1);

		if (productIds == null || productIds.isEmpty()) {
			log.debug("No recent view IDs found for user: {}", userId);
			return new ArrayList<>();
		}

		List<Long> ids = productIds.stream()
			.map(obj -> Long.parseLong(obj.toString()))
			.collect(Collectors.toList());

		log.info("Returning {} recent view IDs for user: {}", ids.size(), userId);
		return ids;
	}

}
