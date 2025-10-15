package com.example.mini_shop.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.mini_shop.config.RabbitMQConfig;
import com.example.mini_shop.event.StockUpdatedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class StockEventListener {

	@RabbitListener(queues = RabbitMQConfig.STOCK_QUEUE)
	public void handleStockUpdated(StockUpdatedEvent event) {
		log.info("Received StockUpdatedEvent: productId={}, previousStock={}, currentStock={}",
			event.getProductId(), event.getPreviousStock(), event.getCurrentStock());

		try {
			// 재고 변동 알림 로직
			if (event.getCurrentStock() < 5) {
				log.warn("Low stock alert: productId={}, currentStock={}",
					event.getProductId(), event.getCurrentStock());
				// 재고 부족 알림 발송 (관리자에게)
			}

			// 재고 히스토리 저장 등
			log.debug("Stock event processed: productId={}", event.getProductId());

		} catch (Exception e) {
			log.error("Failed to process StockUpdatedEvent: {}", e.getMessage(), e);
			// 재고 이벤트 처리 실패는 재시도
			throw new RuntimeException("Stock event processing failed", e);
		}
	}
}
