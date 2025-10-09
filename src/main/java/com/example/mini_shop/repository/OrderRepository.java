package com.example.mini_shop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.mini_shop.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByCustomerEmailOrderByCreatedAtDesc(String mail);
	List<Order> findByStatus(Order.OrderStatus status);
}
