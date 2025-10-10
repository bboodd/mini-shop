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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
		log.info("API Request: POST /api/orders - userId: {}, customer: {}, email: {}",
			request.getUserId(), request.getCustomerName(), request.getCustomerEmail());

		try {
			Order order = orderService.createOrder(
				request.getUserId(),
				request.getCustomerName(),
				request.getCustomerEmail()
			);
			log.info("API Response: Order created successfully with id: {}, totalAmount: {}",
				order.getId(), order.getTotalAmount());
			return ResponseEntity.status(HttpStatus.CREATED).body(order);
		} catch (Exception e) {
			log.error("API Error: Failed to create order - {}", e.getMessage());
			throw e;
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<Order> getOrder(@PathVariable Long id) {
		log.info("API Request: GET /api/orders/{}", id);
		Order order = orderService.getOrder(id);
		log.info("API Response: Order {} retrieved - status: {}, items: {}",
			id, order.getStatus(), order.getOrderItems().size());
		return ResponseEntity.ok(order);
	}

	@GetMapping("/customer/{email}")
	public ResponseEntity<List<Order>> getOrdersByEmail(@PathVariable String email) {
		log.info("API Request: GET /api/orders/customer/{}", email);
		List<Order> orders = orderService.getOrdersByEmail(email);
		log.info("API Response: Found {} orders for customer: {}", orders.size(), email);
		return ResponseEntity.ok(orders);
	}

	@GetMapping
	public ResponseEntity<List<Order>> getAllOrders() {
		log.info("API Request: GET /api/orders - Get all orders");
		List<Order> orders = orderService.getAllOrders();
		log.info("API Response: Returning {} orders", orders.size());
		return ResponseEntity.ok(orders);
	}

	@PatchMapping("/{id}/status")
	public ResponseEntity<Order> updateOrderStatus(
		@PathVariable Long id,
		@RequestParam Order.OrderStatus status) {
		log.info("API Request: PATCH /api/orders/{}/status - newStatus: {}", id, status);
		Order order = orderService.updateOrderStatus(id, status);
		log.info("API Response: Order {} status updated to {}", id, status);
		return ResponseEntity.ok(order);
	}
}
