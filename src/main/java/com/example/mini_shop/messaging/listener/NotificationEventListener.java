package com.example.mini_shop.messaging.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.example.mini_shop.config.RabbitMQConfig;
import com.example.mini_shop.event.NotificationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class NotificationEventListener {

	@RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
	public void handleNotification(NotificationEvent event) {
		log.info("Received NotificationEvent: type={}, recipient={}, title={}",
			event.getType(), event.getRecipient(), event.getTitle());

		try {
			// 알림 발송 로직
			switch (event.getType()) {
				case "EMAIL":
					sendEmail(event);
					break;
				case "SMS":
					sendSMS(event);
					break;
				case "PUSH":
					sendPush(event);
					break;
				default:
					log.warn("Unknown notification type: {}", event.getType());
			}

			log.info("Notification sent successfully: type={}, recipient={}",
				event.getType(), event.getRecipient());

		} catch (Exception e) {
			log.error("Failed to send notification: {}", e.getMessage(), e);
			throw new RuntimeException("Notification failed", e);
		}
	}

	private void sendEmail(NotificationEvent event) {
		log.info("Sending email to: {}, title: {}", event.getRecipient(), event.getTitle());
		// 실제 이메일 발송 로직 (JavaMailSender 등)
		// 현재는 로그만 출력
	}

	private void sendSMS(NotificationEvent event) {
		log.info("Sending SMS to: {}, message: {}", event.getRecipient(), event.getMessage());
		// 실제 SMS 발송 로직
	}

	private void sendPush(NotificationEvent event) {
		log.info("Sending Push to: {}, title: {}", event.getRecipient(), event.getTitle());
		// 실제 푸시 알림 발송 로직
	}
}
