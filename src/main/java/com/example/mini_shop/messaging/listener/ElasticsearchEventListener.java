package com.example.mini_shop.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.mini_shop.config.RabbitMQConfig;
import com.example.mini_shop.document.ProductDocument;
import com.example.mini_shop.event.ElasticsearchIndexEvent;
import com.example.mini_shop.repository.ProductRepository;
import com.example.mini_shop.repository.ProductSearchRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class ElasticsearchEventListener {

	private final ProductRepository productRepository;
	private final ProductSearchRepository productSearchRepository;

	@RabbitListener(queues = RabbitMQConfig.ELASTICSEARCH_QUEUE)
	public void handleElasticsearchIndex(ElasticsearchIndexEvent event) {
		log.info("Received ElasticsearchIndexEvent: productId={}, operation={}",
			event.getProductId(), event.getOperation());

		try {
			switch (event.getOperation()) {
				case "INDEX":
				case "UPDATE":
					indexProduct(event.getProductId());
					break;
				case "DELETE":
					deleteProduct(event.getProductId());
					break;
				default:
					log.warn("Unknown operation: {}", event.getOperation());
			}

			log.info("Elasticsearch operation completed: productId={}, operation={}",
				event.getProductId(), event.getOperation());

		} catch (Exception e) {
			log.error("Failed to process Elasticsearch event: {}", e.getMessage(), e);
			throw new RuntimeException("Elasticsearch indexing failed", e);
		}
	}

	private void indexProduct(Long productId) {
		productRepository.findById(productId).ifPresent(product -> {
			ProductDocument document = ProductDocument.from(product);
			productSearchRepository.save(document);
			log.info("Product indexed in Elasticsearch: productId={}", productId);
		});
	}

	private void deleteProduct(Long productId) {
		productSearchRepository.deleteById(String.valueOf(productId));
		log.info("Product deleted from Elasticsearch: productId={}", productId);
	}
}
