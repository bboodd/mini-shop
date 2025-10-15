package com.example.mini_shop.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.mini_shop.entity.Order;
import com.example.mini_shop.entity.OrderItem;
import com.example.mini_shop.entity.Product;
import com.example.mini_shop.event.NotificationEvent;
import com.example.mini_shop.event.OrderCreatedEvent;
import com.example.mini_shop.messaging.EventPublisher;
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
	private final EventPublisher eventPublisher;

	@Transactional
	public Order createOrder(String userId, String customerName, String customerEmail) {
		log.info("Creating order for user: {}", userId);
		List<CartItem> cartItems = cartService.getCart(userId);

		if (cartItems.isEmpty()) {
			log.error("Cannot create order: cart is empty for user: {}", userId);
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
				log.error("Insufficient stock: productId={}, requested={}, available={}",
					cartItem.getProductId(), cartItem.getQuantity(), product.getStock());
				throw new RuntimeException("Insufficient stock for product: " + product.getName());
			}

			// 재고 감소 (decreaseStock 메서드 사용)
			productService.decreaseStock(product.getId(), cartItem.getQuantity());

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

		log.info("Order created successfully: orderId={}, totalAmount={}",
			savedOrder.getId(), savedOrder.getTotalAmount());

		// 주문 생성 이벤트 발행
		// TODO:orderItems 이벤트 dto로 바꾸는 로직 필요
		eventPublisher.publishOrderCreated(
			OrderCreatedEvent.builder()
				.orderId(savedOrder.getId())
				.customerName(savedOrder.getCustomerName())
				.customerEmail(savedOrder.getCustomerEmail())
				.totalAmount(savedOrder.getTotalAmount())
				.createdAt(LocalDateTime.now())
				.build()
		);

		// 알림 이벤트 발행
		eventPublisher.publishNotification(
			NotificationEvent.builder()
				.recipient(savedOrder.getCustomerEmail())
				.type("ORDER_CREATED")
				.title("Order Created")
				.message(String.format("Order #%d has been created successfully", savedOrder.getId()))
				.createdAt(LocalDateTime.now())
				.build()
		);

		// 장바구니 비우기
		cartService.clearCart(userId);
		log.info("Cart cleared for user: {}", userId);

		return savedOrder;
	}

	public Order getOrder(Long orderId) {
		log.debug("Fetching order with id: {}", orderId);
		return orderRepository.findById(orderId)
			.orElseThrow(() -> {
				log.error("Order not found with id: {}", orderId);
				return new RuntimeException("Order not found");
			});
	}

	public List<Order> getOrdersByEmail(String email) {
		log.info("Fetching orders for email: {}", email);
		List<Order> orders = orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
		log.info("Found {} orders for email: {}", orders.size(), email);
		return orders;
	}

	public List<Order> getAllOrders() {
		log.info("Fetching all orders");
		List<Order> orders = orderRepository.findAll();
		log.info("Found {} orders", orders.size());
		return orders;
	}

	@Transactional
	public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
		log.info("Updating order status: orderId={}, newStatus={}", orderId, status);
		Order order = getOrder(orderId);
		Order.OrderStatus previousStatus = order.getStatus();
		order.setStatus(status);
		Order updatedOrder = orderRepository.save(order);
		log.info("Order status updated successfully: orderId={}, previousStatus={}, currentStatus={}",
			orderId, previousStatus, status);

		// 상태 변경 알림 이벤트 발행
		eventPublisher.publishNotification(
			NotificationEvent.builder()
				.recipient(updatedOrder.getCustomerEmail())
				.type("ORDER_STATUS_UPDATED")
				.title("Order Status Updated")
				.message(String.format("Order #%d status has been updated to %s",
					updatedOrder.getId(), status))
				.createdAt(LocalDateTime.now())
				.build()
		);

		return updatedOrder;
	}
}
