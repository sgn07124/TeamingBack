package com.project.Teaming.global.messageQueue.publisher;

import com.project.Teaming.global.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE_NAME = "notification.exchange";

    @Transactional(readOnly = true)
    public void sendNotificationEvent(NotificationEvent event) {
        log.info("📢 RabbitMQ 알림 이벤트 발행: {}", event.getNotificationIds());

        try {
            rabbitTemplate.convertAndSend(EXCHANGE_NAME, "", event);
            log.info("🚀 RabbitMQ 메시지 발행 완료");
        } catch (Exception e) {
            log.error("❌ RabbitMQ 메시지 발행 실패: {}", e.getMessage(), e);
        }
    }
}