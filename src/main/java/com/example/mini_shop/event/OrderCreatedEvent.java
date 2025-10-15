package com.example.mini_shop.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long orderId;
	private String customerName;
	private String customerEmail;
	private BigDecimal totalAmount;
	private List<OrderItemData> items;
	private LocalDateTime createdAt;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class OrderItemData implements Serializable {
		private Long productId;
		private String productName;
		private Integer quantity;
		private BigDecimal price;
	}
}
