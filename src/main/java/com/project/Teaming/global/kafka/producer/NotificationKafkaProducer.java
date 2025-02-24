package com.project.Teaming.global.kafka.producer;

import com.project.Teaming.global.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private static final String NOTIFICATION_TOPIC = "notification-events";

    public void sendNotificationEvent(NotificationEvent event) {
        log.info("📢 Kafka 알림 이벤트 발행: {}", event.getNotificationIds());
        kafkaTemplate.send(NOTIFICATION_TOPIC, event);
    }
}
