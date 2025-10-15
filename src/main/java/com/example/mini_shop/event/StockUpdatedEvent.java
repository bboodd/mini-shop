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
public class StockUpdatedEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long productId;
	private String productName;
	private Integer previousStock;
	private Integer currentStock;
	private String operation; // DECREASE, INCREASE
	private LocalDateTime updatedAt;
}
