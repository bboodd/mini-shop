package com.example.mini_shop.event;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ElasticsearchIndexEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long productId;
	private String operation; // INDEX, UPDATE, DELETE
	private LocalDateTime timestamp;
}
