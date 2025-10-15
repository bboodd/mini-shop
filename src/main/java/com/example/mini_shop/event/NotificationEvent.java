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
public class NotificationEvent implements Serializable {
	private static final long serialVersionUID = 1L;

	private String recipient; // email or userId
	private String type; // EMAIL, SMS, PUSH
	private String title;
	private String message;
	private LocalDateTime createdAt;
}
