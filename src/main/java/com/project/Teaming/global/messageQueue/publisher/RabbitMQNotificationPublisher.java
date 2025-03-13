package com.project.Teaming.global.messageQueue.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.Teaming.global.event.NotificationEvent;
import com.project.Teaming.global.sse.dto.EventPayload;
import com.project.Teaming.global.sse.dto.EventWithTeamPayload;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitMQNotificationPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE_NAME = "notification.exchange";
    private final StringRedisTemplate stringRedisTemplate;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public void sendNotificationEvent(NotificationEvent event) {
        log.info("📢 RabbitMQ 알림 이벤트 발행: {}", event.getNotificationIds());

        List<Notification> notifications = notificationRepository.findAllById(event.getNotificationIds());
        if (notifications.isEmpty()) {
            log.warn("⚠️ 해당 알림 ID들에 대한 알림 없음 - 전송 중단");
            return;
        }

        notifications.forEach(notification -> {
            Long userId = notification.getUser().getId();
            String targetServerId = stringRedisTemplate.opsForValue().get("sse_server:" + userId);

            // ✅ Redis에 서버 정보가 없으면 "오프라인 사용자 큐"에 저장 가능
            if (targetServerId == null) {
                log.warn("⚠️ Redis에 사용자({})의 SSE 연결 정보 없음 - 알림 전송 중단", userId);
                return;
            }

            try {
                Object payload;
                if (notification.getTeamId() != null) {
                    payload = EventWithTeamPayload.builder()
                            .userId(userId)
                            .type(notification.getType())
                            .category(notification.getCategory())
                            .teamId(notification.getTeamId())
                            .createdAt(notification.getCreatedAt().toString())
                            .message(notification.getMessage())
                            .isRead(notification.isRead())
                            .build();
                } else {
                    payload = EventPayload.builder()
                            .userId(userId)
                            .type(notification.getType())
                            .category(notification.getCategory())
                            .createdAt(notification.getCreatedAt().toString())
                            .message(notification.getMessage())
                            .isRead(notification.isRead())
                            .build();
                }
                String jsonPayload = objectMapper.writeValueAsString(payload);

                // ✅ 올바른 서버의 큐로 메시지 전송
                log.info("🚀 RabbitMQ 메시지 발행 → 서버: {} | User: {} | 알림 ID: {}", targetServerId, userId, notification.getId());
                rabbitTemplate.convertAndSend(EXCHANGE_NAME, targetServerId, jsonPayload);
            } catch (Exception e) {
                log.error("❌ RabbitMQ 메시지 변환 실패: {}", e.getMessage(), e);
            }
        });
    }
}
