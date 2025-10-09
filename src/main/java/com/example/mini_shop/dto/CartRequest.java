package com.example.mini_shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequest {
	private String userId;
	private Long productId;
	private Integer quantity;
}
