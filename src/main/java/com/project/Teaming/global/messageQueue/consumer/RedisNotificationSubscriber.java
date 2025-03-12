package com.project.Teaming.global.messageQueue.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.Teaming.global.event.NotificationEvent;
import com.project.Teaming.global.sse.dto.EventPayload;
import com.project.Teaming.global.sse.dto.EventWithTeamPayload;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.entity.NotificationType;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import com.project.Teaming.global.sse.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisNotificationSubscriber implements MessageListener {

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;
    private final Executor notificationExecutor;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
            NotificationEvent event = objectMapper.readValue(messageBody, NotificationEvent.class);

            log.info("✅ Redis Pub/Sub 알림 이벤트 수신: {}", event.getNotificationIds());

            // ID 기반으로 Notification 객체 조회
            List<Notification> notifications = notificationRepository.findAllById(event.getNotificationIds());

            notifications.forEach(notification -> {
                if (shouldProcessMultiThreading(notification)) {
                    CompletableFuture.runAsync(() -> sendWithMultiThreading(notification), notificationExecutor);
                } else {
                    sendWithoutMultiThreading(notification);
                }
            });

            log.info("✅ Redis Pub/Sub 알림 이벤트 처리 완료!");
        } catch (Exception e) {
            log.error("❌ Redis Pub/Sub 알림 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }

    private boolean shouldProcessMultiThreading(Notification notification) {
        return
                (notification.getType().equals(NotificationType.MENTORING_EXPORT.getTitle()) || notification.getType().equals(NotificationType.MENTORING_DELETE.getTitle())
                        || notification.getType().equals(NotificationType.PROJECT_TEAM_QUIT.getTitle()) || notification.getType().equals(NotificationType.PROJECT_TEAM_EXPORT.getTitle()));
    }

    private void sendWithMultiThreading(Notification notification) {
        try {
            sseEmitterService.sendWithTeamIdWithOutAsync(
                    notification.getUser().getId(),
                    EventWithTeamPayload.builder()
                            .userId(notification.getUser().getId())
                            .type(notification.getType())
                            .category(notification.getCategory())
                            .teamId(notification.getTeamId())
                            .createdAt(notification.getCreatedAt().toString())
                            .message(notification.getMessage())
                            .isRead(notification.isRead())
                            .build());
        } catch (Exception e) {
            log.error("❌ SSE 알림 전송 실패 : {}", e.getMessage(), e);
        }
    }

    private void sendWithoutMultiThreading(Notification notification) {
        try {
            if (notification.getTeamId() != null) {
                sseEmitterService.sendWithTeamId(
                        notification.getUser().getId(),
                        EventWithTeamPayload.builder()
                                .userId(notification.getUser().getId())
                                .type(notification.getType())
                                .category(notification.getCategory())
                                .teamId(notification.getTeamId())
                                .createdAt(notification.getCreatedAt().toString())
                                .message(notification.getMessage())
                                .isRead(notification.isRead())
                                .build());
            } else {
                sseEmitterService.send(notification.getUser().getId(),
                        EventPayload.builder()
                                .userId(notification.getUser().getId())
                                .type(notification.getType())
                                .type(notification.getCategory())
                                .createdAt(notification.getCreatedAt().toString())
                                .message(notification.getMessage())
                                .isRead(notification.isRead())
                                .build());
            }
        } catch (Exception e) {
            log.error("❌ SSE 알림 전송 실패 : {}", e.getMessage(), e);
        }
    }
}
