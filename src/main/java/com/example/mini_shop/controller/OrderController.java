package com.example.mini_shop.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.mini_shop.dto.OrderRequest;
import com.example.mini_shop.entity.Order;
import com.example.mini_shop.service.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
		Order order = orderService.createOrder(
			request.getUserId(),
			request.getCustomerName(),
			request.getCustomerEmail()
		);
		return ResponseEntity.status(HttpStatus.CREATED).body(order);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Order> getOrder(@PathVariable Long id) {
		return ResponseEntity.ok(orderService.getOrder(id));
	}

	@GetMapping("/customer/{email}")
	public ResponseEntity<List<Order>> getOrdersByEmail(@PathVariable String email) {
		return ResponseEntity.ok(orderService.getOrdersByEmail(email));
	}

	@GetMapping
	public ResponseEntity<List<Order>> getAllOrders() {
		return ResponseEntity.ok(orderService.getAllOrders());
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<Order> updateOrderStatus(
		@PathVariable Long id,
		@RequestParam Order.OrderStatus status
	) {
		return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
	}
}
