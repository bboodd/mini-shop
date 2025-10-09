package com.example.mini_shop.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.mini_shop.entity.Product;
import com.example.mini_shop.service.ProductService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private final ProductService productService;

	@Override
	public void run(String... args) {
		// 샘플 상품 데이터 생성
		createProduct("맥북 프로 14인치", "M3 칩셋, 16GB RAM, 512GB SSD",
			new BigDecimal("2890000"), 10, "전자기기");

		createProduct("아이폰 15 Pro", "256GB, 티타늄 블루",
			new BigDecimal("1550000"), 15, "전자기기");

		createProduct("에어팟 프로 2세대", "노이즈 캔슬링, USB-C",
			new BigDecimal("359000"), 25, "전자기기");

		createProduct("나이키 에어맥스", "운동화, 사이즈 270",
			new BigDecimal("189000"), 20, "신발");

		createProduct("아디다스 트레이닝복", "상하의 세트, L사이즈",
			new BigDecimal("129000"), 30, "의류");

		createProduct("삼성 갤럭시 워치", "스마트워치, 블랙",
			new BigDecimal("389000"), 12, "전자기기");

		createProduct("LG 그램 노트북", "15.6인치, 1.1kg 초경량",
			new BigDecimal("1890000"), 8, "전자기기");

		createProduct("소니 WH-1000XM5", "무선 헤드폰, 노이즈 캔슬링",
			new BigDecimal("449000"), 18, "전자기기");

		createProduct("노스페이스 패딩", "구스다운, 겨울용",
			new BigDecimal("459000"), 15, "의류");

		createProduct("뉴발란스 530", "레트로 스니커즈",
			new BigDecimal("139000"), 22, "신발");
	}

	private void createProduct(String name, String description,
		BigDecimal price, Integer stock, String category) {
		Product product = Product.builder()
			.name(name)
			.description(description)
			.price(price)
			.stock(stock)
			.category(category)
			.build();

		productService.createProduct(product);
	}
}
