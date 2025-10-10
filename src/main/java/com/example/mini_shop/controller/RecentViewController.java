package com.example.mini_shop.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mini_shop.entity.Product;
import com.example.mini_shop.service.RecentViewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/recent-views")
@RequiredArgsConstructor
public class RecentViewController {

	private final RecentViewService recentViewService;

	/**
	 * 최근 본 상품 추가
	 */
	@PostMapping
	public ResponseEntity<Void> addRecentView(
		@RequestParam String userId,
		@RequestParam Long productId) {
		recentViewService.addRecentView(userId, productId);
		return ResponseEntity.ok().build();
	}

	/**
	 * 최근 본 상품 목록 조회
	 */
	@GetMapping("/{userId}")
	public ResponseEntity<List<Product>> getRecentViews(@PathVariable String userId) {
		return ResponseEntity.ok(recentViewService.getRecentViews(userId));
	}

	/**
	 * 최근 본 상품 개수 조회
	 */
	@GetMapping("/{userId}/count")
	public ResponseEntity<Long> getRecentViewCount(@PathVariable String userId) {
		return ResponseEntity.ok(recentViewService.getRecentViewCount(userId));
	}

	/**
	 * 최근 본 상품 ID 목록 조회
	 */
	@GetMapping("/{userId}/ids")
	public ResponseEntity<List<Long>> getRecentViewIds(@PathVariable String userId) {
		return ResponseEntity.ok(recentViewService.getRecentViewIds(userId));
	}

	/**
	 * 특정 상품 제거
	 */
	@DeleteMapping("/{userId}/{productId}")
	public ResponseEntity<Void> removeRecentView(
		@PathVariable String userId,
		@PathVariable Long productId) {
		recentViewService.removeRecentView(userId, productId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * 전체 삭제
	 */
	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> clearRecentViews(@PathVariable String userId) {
		recentViewService.clearRecentViews(userId);
		return ResponseEntity.noContent().build();
	}
}
