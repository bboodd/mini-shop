package com.example.mini_shop;

import org.springframework.boot.SpringApplication;

public class TestMiniShopApplication {

	public static void main(String[] args) {
		SpringApplication.from(MiniShopApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
