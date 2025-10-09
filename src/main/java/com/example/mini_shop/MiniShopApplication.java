package com.example.mini_shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MiniShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniShopApplication.class, args);
	}

}
