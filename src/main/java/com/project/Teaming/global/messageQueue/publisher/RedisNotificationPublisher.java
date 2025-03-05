package com.project.Teaming.global.messageQueue.publisher;

import com.project.Teaming.global.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationPublisher {

    private final RedisTemplate<String, NotificationEvent> redisPubSubTemplate;
    private static final String NOTIFICATION_CHANNEL = "notification-channel";

    public void sendNotificationEvent(NotificationEvent event) {
        log.info("📢 Redis 알림 이벤트 발행: {}", event.getNotificationIds());
        redisPubSubTemplate.convertAndSend(NOTIFICATION_CHANNEL, event);
    }

}
