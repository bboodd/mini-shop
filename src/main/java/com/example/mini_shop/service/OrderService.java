package com.example.mini_shop.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mini_shop.entity.Order;
import com.example.mini_shop.entity.OrderItem;
import com.example.mini_shop.entity.Product;
import com.example.mini_shop.model.CartItem;
import com.example.mini_shop.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

	private final OrderRepository orderRepository;
	private final CartService cartService;
	private final ProductService productService;

	@Transactional
	public Order createOrder(String userId, String customerName, String customerEmail) {
		log.info("Creating order for user: {}", userId);
		List<CartItem> cartItems = cartService.getCart(userId);

		if (cartItems.isEmpty()) {
			throw new RuntimeException("Cart is empty");
		}

		Order order = Order.builder()
			.customerName(customerName)
			.customerEmail(customerEmail)
			.totalAmount(BigDecimal.ZERO)
			.build();

		BigDecimal totalAmount = BigDecimal.ZERO;

		for (CartItem cartItem : cartItems) {
			Product product = productService.getProduct(cartItem.getProductId());

			if (product.getStock() < cartItem.getQuantity()) {
				throw new RuntimeException("Insufficient stock for product: " + product.getName());
			}

			// 재고 감소
			product.setStock(product.getStock() - cartItem.getQuantity());
			productService.updateProduct(product.getId(), product);

			OrderItem orderItem = OrderItem.builder()
				.product(product)
				.quantity(cartItem.getQuantity())
				.price(cartItem.getPrice())
				.build();

			order.addOrderItem(orderItem);
			totalAmount = totalAmount.add(cartItem.getTotalPrice());
		}

		order.setTotalAmount(totalAmount);
		Order savedOrder = orderRepository.save(order);

		log.info("Order created successfully: {}", savedOrder.getId());

		// 장바구니 비우기
		cartService.clearCart(userId);

		return savedOrder;
	}

	public Order getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> new RuntimeException("Order not found"));
	}

	public List<Order> getOrdersByEmail(String email) {
		return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
	}

	public List<Order> getAllOrders() {
		return orderRepository.findAll();
	}

	@Transactional
	public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
		Order order = getOrder(orderId);
		order.setStatus(status);
		return orderRepository.save(order);
	}
}
