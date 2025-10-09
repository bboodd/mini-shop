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

@RestController
@RequestMapping("/api/recent-views")
@RequiredArgsConstructor
public class RecentViewController {

	private final RecentViewService recentViewService;

	@PostMapping
	public ResponseEntity<Void> addRecentView(
		@RequestParam String userId,
		@RequestParam Long productId
	) {
		recentViewService.addRecentView(userId, productId);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/{userId}")
	public ResponseEntity<List<Product>> getRecentViews(@PathVariable String userId) {
		return ResponseEntity.ok(recentViewService.getRecentViews(userId));
	}

	@GetMapping("/{userId}/count")
	public ResponseEntity<Long> getRecentViewCount(@PathVariable String userId) {
		return ResponseEntity.ok(recentViewService.getRecentViewCount(userId));
	}

	@GetMapping("/{userId}/ids")
	public ResponseEntity<List<Long>> getRecentViewIds(@PathVariable String userId) {
		return ResponseEntity.ok(recentViewService.getRecentViewIds(userId));
	}

	@DeleteMapping("{userId}/{productId}")
	public ResponseEntity<Void> removeRecentView(
		@PathVariable String userId,
		@PathVariable Long productId
	) {
		recentViewService.removeRecentView(userId, productId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> clearRecentViews(@PathVariable String userId) {
		recentViewService.clearRecentViews(userId);
		return ResponseEntity.noContent().build();
	}
}
