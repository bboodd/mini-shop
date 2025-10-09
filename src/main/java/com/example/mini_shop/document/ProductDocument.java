package com.example.mini_shop.document;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import com.example.mini_shop.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Document(indexName = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDocument {

	@Id
	private String id;

	@Field(type = FieldType.Text, analyzer = "standard")
	private String name;

	@Field(type = FieldType.Text)
	private String description;

	@Field(type = FieldType.Double)
	private BigDecimal price;

	@Field(type = FieldType.Integer)
	private Integer stock;

	@Field(type = FieldType.Keyword)
	private String category;

	public static ProductDocument from(Product product) {
		return ProductDocument.builder()
			.id(String.valueOf(product.getId()))
			.name(product.getName())
			.description(product.getDescription())
			.price(product.getPrice())
			.stock(product.getStock())
			.category(product.getCategory())
			.build();
	}
}
