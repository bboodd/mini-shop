package com.example.mini_shop.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig {

	@Bean
	public ObjectMapper redisObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		// Java 8 날짜/시간 타입 지원
		mapper.registerModule(new JavaTimeModule());
		// timestamp 형식 대신 ISO-8601 형식 사용
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// // 다형성 타입 처리를 위한 설정 (LinkedHashMap 문제 해결)
		// PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
		// 	.allowIfBaseType(Object.class)
		// 	.build();
		// mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);

		// 타입 정보 포함하지 않음 (깔끔한 JSON)
		mapper.deactivateDefaultTyping();

		return mapper;
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// Key serializer
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());

		// Value serializer - 타입정보 포함하는 ObjectMapper 사용
		GenericJackson2JsonRedisSerializer serializer =
			new GenericJackson2JsonRedisSerializer(redisObjectMapper());
		template.setValueSerializer(serializer);
		template.setHashValueSerializer(serializer);

		template.afterPropertiesSet();
		return template;
	}

	@Bean
	public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
		GenericJackson2JsonRedisSerializer serializer =
			new GenericJackson2JsonRedisSerializer(redisObjectMapper());

		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofMinutes(10))
			.serializeKeysWith(
				RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
			)
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(serializer)
			);

		return RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(config)
			.build();
	}
}
