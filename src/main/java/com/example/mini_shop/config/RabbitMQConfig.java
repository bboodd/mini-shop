package com.example.mini_shop.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

	// Exchange 이름
	public static final String SHOP_EXCHANGE = "shop.exchange";

	// Queue 이름
	public static final String ORDER_QUEUE = "order.queue";
	public static final String STOCK_QUEUE = "stock.queue";
	public static final String NOTIFICATION_QUEUE = "notification.queue";
	public static final String ELASTICSEARCH_QUEUE = "elasticsearch.queue";

	// Routing Key
	public static final String ORDER_ROUTING_KEY = "order.created";
	public static final String STOCK_ROUTING_KEY = "stock.updated";
	public static final String NOTIFICATION_ROUTING_KEY = "notification.send";
	public static final String ELASTICSEARCH_ROUTING_KEY = "elasticsearch.index";

	// Dead Letter Queue
	public static final String DLQ_EXCHANGE = "shop.dlq.exchange";
	public static final String DLQ_QUEUE = "shop.dlq.queue";
	public static final String DLQ_ROUTING_KEY = "dlq";

	/**
	 * JSON Message Converter
	 */
	@Bean
	public MessageConverter jsonMessageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	/**
	 * RabbitTemplate with JSON Converter
	 */
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(jsonMessageConverter());
		return template;
	}

	/**
	 * Listener Container Factory
	 */
	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(connectionFactory);
		factory.setMessageConverter(jsonMessageConverter());
		factory.setConcurrentConsumers(3);
		factory.setMaxConcurrentConsumers(10);
		return factory;
	}

	/**
	 * Main Exchange (Topic)
	 */
	@Bean
	public TopicExchange shopExchange() {
		return new TopicExchange(SHOP_EXCHANGE, true, false);
	}

	/**
	 * Dead Letter Exchange
	 */
	@Bean
	public DirectExchange dlqexchange() {
		return new DirectExchange(DLQ_EXCHANGE, true, false);
	}

	/**
	 * Order Queue
	 */
	@Bean
	public Queue orderQueue() {
		return QueueBuilder.durable(ORDER_QUEUE)
			.withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
			.withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
			.build();
	}

	/**
	 * Stock Queue
	 */
	@Bean
	public Queue stockQueue() {
		return QueueBuilder.durable(STOCK_QUEUE)
			.withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
			.withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
			.build();
	}

	/**
	 * Notification Queue
	 */
	@Bean
	public Queue notificationQueue() {
		return QueueBuilder.durable(NOTIFICATION_QUEUE)
			.withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
			.withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
			.build();
	}

	/**
	 * Elasticsearch Queue
	 */
	@Bean
	public Queue elasticsearchQueue() {
		return QueueBuilder.durable(ELASTICSEARCH_QUEUE)
			.withArgument("x-dead-letter-exchange", DLQ_EXCHANGE)
			.withArgument("x-dead-letter-routing-key", DLQ_ROUTING_KEY)
			.build();
	}

	/**
	 * Dead Letter Queue
	 */
	@Bean
	public Queue dlqQueue() {
		return QueueBuilder.durable(DLQ_QUEUE).build();
	}

	/**
	 * Bindings
	 */
	@Bean
	public Binding orderBinding(Queue orderQueue, TopicExchange shopExchange) {
		return BindingBuilder.bind(orderQueue)
			.to(shopExchange)
			.with(ORDER_ROUTING_KEY);
	}

	@Bean
	public Binding stockBinding(Queue stockQueue, TopicExchange shopExchange) {
		return BindingBuilder.bind(stockQueue)
			.to(shopExchange)
			.with(STOCK_ROUTING_KEY);
	}

	@Bean
	public Binding notificationBinding(Queue notificationQueue, TopicExchange shopExchange) {
		return BindingBuilder.bind(notificationQueue)
			.to(shopExchange)
			.with(NOTIFICATION_ROUTING_KEY);
	}

	@Bean
	public Binding elasticsearchBinding(Queue elasticsearchQueue, TopicExchange shopExchange) {
		return BindingBuilder.bind(elasticsearchQueue)
			.to(shopExchange)
			.with(ELASTICSEARCH_ROUTING_KEY);
	}

	@Bean
	public Binding dlqBinding(Queue dlqQueue, DirectExchange dlqExchange) {
		return BindingBuilder.bind(dlqQueue)
			.to(dlqExchange)
			.with(DLQ_ROUTING_KEY);
	}

}
