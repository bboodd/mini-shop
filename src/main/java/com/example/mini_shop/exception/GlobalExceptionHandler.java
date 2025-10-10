package com.example.mini_shop.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(RuntimeException.class)
	public ResponseEntity<Map<String, Object>> handleRuntimeException(
		RuntimeException ex, WebRequest request) {

		log.error("RuntimeException occurred: {} - Request: {}",
			ex.getMessage(), request.getDescription(false), ex);

		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("timestamp", LocalDateTime.now());
		errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
		errorResponse.put("error", "Bad Request");
		errorResponse.put("message", ex.getMessage());
		errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
		IllegalArgumentException ex, WebRequest request) {

		log.error("IllegalArgumentException occurred: {} - Request: {}",
			ex.getMessage(), request.getDescription(false));

		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("timestamp", LocalDateTime.now());
		errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
		errorResponse.put("error", "Invalid Argument");
		errorResponse.put("message", ex.getMessage());
		errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGlobalException(
		Exception ex, WebRequest request) {

		log.error("Unexpected exception occurred: {} - Request: {}",
			ex.getMessage(), request.getDescription(false), ex);

		Map<String, Object> errorResponse = new HashMap<>();
		errorResponse.put("timestamp", LocalDateTime.now());
		errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		errorResponse.put("error", "Internal Server Error");
		errorResponse.put("message", "An unexpected error occurred");
		errorResponse.put("path", request.getDescription(false).replace("uri=", ""));

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
	}
}
