package com.example.mini_shop.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.mini_shop.config.RabbitMQConfig;
import com.example.mini_shop.event.OrderCreatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

	@RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
	public void handleOrderCreated(OrderCreatedEvent event) {
		log.info("Received OrderCreatedEvent: orderId={}, customerEmail={}",
			event.getOrderId(), event.getCustomerEmail());

		try {
			// 주문 처리 로직 (예: 주문 상태 업데이트, 외부 시스템 연동 등)
			log.info("Processing order: orderId={}, totalAmount={}",
				event.getOrderId(), event.getTotalAmount());

			// 주문 처리 완료
			log.info("Order processed successfully: orderId={}", event.getOrderId());
		} catch (Exception e) {
			log.error("Failed to process OrderCreatedEvent: {}", e.getMessage(), e);
			throw new RuntimeException("Order processing failed", e);
		}
	}
}
