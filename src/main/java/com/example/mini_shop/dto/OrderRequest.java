package com.example.mini_shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
	private String userId;
	private String customerName;
	private String customerEmail;
}
