package com.example.mini_shop.entity;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	@JsonBackReference // 역방향: 직렬화 X (차단)
	private Order order;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(nullable = false)
	private Integer quantity;

	@Column(nullable = false)
	private BigDecimal price;

	public BigDecimal getSubTotal() {
		return price.multiply(BigDecimal.valueOf(quantity));
	}
}
