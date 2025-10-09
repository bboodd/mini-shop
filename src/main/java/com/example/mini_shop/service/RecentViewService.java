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
		String key = RECENT_VIEW_KEY_PREFIX + userId;
		double score = System.currentTimeMillis();

		// ZSet에 추가 ( 중복시 score만 업데이트 )
		redisTemplate.opsForZSet().add(key, productId.toString(), score);

		// 최대 개수 초과 시 오래된 항목 삭제
		Long size = redisTemplate.opsForZSet().size(key);
		if (size != null && size > MAX_RECENT_ITEMS) {
			redisTemplate.opsForZSet().removeRange(key, 0, size - MAX_RECENT_ITEMS - 1);
		}

		// TTL
		redisTemplate.expire(key, TTL_DAYS, TimeUnit.DAYS);
	}

	/**
	 * 최근 본 상품 목록 조회 ( 최신순 )
	 */
	public List<Product> getRecentViews(String userId) {
		String key = RECENT_VIEW_KEY_PREFIX + userId;

		// ZSet에서 최신순으로 조회 ( 역순 )
		Set<Object> productIds = redisTemplate.opsForZSet()
			.reverseRange(key, 0, MAX_RECENT_ITEMS - 1);

		if (productIds == null || productIds.isEmpty()) {
			return new ArrayList<>();
		}

		// Products information
		List<Product> recentProducts = new ArrayList<>();
		for (Object obj : productIds) {
			try {
				Long productId = Long.parseLong(obj.toString());
				Product product = productService.getProduct(productId);
				recentProducts.add(product);
			} catch (Exception e) {
				// 상품 삭제 등 예외처리
				continue;
			}
		}

		return recentProducts;
	}

	/**
	 * 최근 본 상품 개수 조회
	 */
	public Long getRecentViewCount(String userId) {
		String key = RECENT_VIEW_KEY_PREFIX + userId;
		Long count = redisTemplate.opsForZSet().size(key);
		return count != null ? count : 0L;
	}

	/**
	 * 특정 상품 제거
	 */
	public void removeRecentView(String userId, Long productId) {
		String key = RECENT_VIEW_KEY_PREFIX + userId;
		redisTemplate.opsForZSet().remove(key, productId.toString());
	}

	/**
	 * 전체 최근 본 상품 삭제
	 */
	public void clearRecentViews(String userId) {
		String key = RECENT_VIEW_KEY_PREFIX + userId;
		redisTemplate.delete(key);
	}

	/**
	 * 최근 본 상품 ID 목록만 조회
	 */
	public List<Long> getRecentViewIds(String userId) {
		String key = RECENT_VIEW_KEY_PREFIX + userId;

		Set<Object> productIds = redisTemplate.opsForZSet()
			.reverseRange(key, 0, MAX_RECENT_ITEMS - 1);

		if (productIds == null || productIds.isEmpty()) {
			return new ArrayList<>();
		}

		return productIds.stream()
			.map(obj -> Long.parseLong(obj.toString()))
			.collect(Collectors.toList());
	}

}
