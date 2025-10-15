package com.example.mini_shop.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.mini_shop.config.RabbitMQConfig;
import com.example.mini_shop.event.ElasticsearchIndexEvent;
import com.example.mini_shop.event.NotificationEvent;
import com.example.mini_shop.event.OrderCreatedEvent;
import com.example.mini_shop.event.StockUpdatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

	private final RabbitTemplate rabbitTemplate;

	/**
	 * 주문 생성 이벤트 발행
	 */
	public void publishOrderCreated(OrderCreatedEvent event) {
		log.info("Puvlishing OrderCreatedEvent: orderId={}", event.getOrderId());
		try {
			rabbitTemplate.convertAndSend(
				RabbitMQConfig.SHOP_EXCHANGE,
				RabbitMQConfig.ORDER_ROUTING_KEY,
				event
			);
			log.info("OrderCreatedEvent published successfully");
		} catch (Exception e) {
			log.error("Failed to publish OrderCreatedEvent: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to publish order event", e);
		}
	}

	/**
	 * 재고 업데이트 이벤트 발행
	 */
	public void publishStockUpdated(StockUpdatedEvent event) {
		log.info("Publishing StockUpdatedEvent: productId={}, stock={}",
			event.getProductId(), event.getCurrentStock());
		try {
			rabbitTemplate.convertAndSend(
				RabbitMQConfig.SHOP_EXCHANGE,
				RabbitMQConfig.STOCK_ROUTING_KEY,
				event
			);
			log.debug("StockUpdatedEvent published successfully");
		} catch (Exception e) {
			log.error("Failed to publish StockUpdatedEvent: {}", e.getMessage(), e);
			// 재고 이벤트 실패는 치명적이지 않으므로 예외를 던지지 않음
		}
	}

	/**
	 * 알림 이벤트 발행
	 */
	public void publishNotification(NotificationEvent event) {
		log.info("Publishing NotificationEvent: type={}, recipient={}",
			event.getType(), event.getRecipient());
		try {
			rabbitTemplate.convertAndSend(
				RabbitMQConfig.SHOP_EXCHANGE,
				RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
				event
			);
			log.debug("NotificationEvent published successfully");
		} catch (Exception e) {
			log.error("Failed to publish NotificationEvent: {}", e.getMessage(), e);
			// 알림 실패는 치명적이지 않으므로 예외를 던지지 않음
		}
	}

	/**
	 * Elasticsearch 인덱싱 이벤트 발행
	 */
	public void publishElasticsearchIndex(ElasticsearchIndexEvent event) {
		log.info("Publishing ElasticsearchIndexEvent: productId={}, operation={}",
			event.getProductId(), event.getOperation());
		try {
			rabbitTemplate.convertAndSend(
				RabbitMQConfig.SHOP_EXCHANGE,
				RabbitMQConfig.ELASTICSEARCH_ROUTING_KEY,
				event
			);
			log.debug("ElasticsearchIndexEvent published successfully");
		} catch (Exception e) {
			log.error("Failed to publish ElasticsearchIndexEvent: {}", e.getMessage(), e);
			// 검색 인덱싱 실패는 치명적이지 않으므로 예외를 던지지 않음
		}
	}
}
