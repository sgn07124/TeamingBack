package com.project.Teaming.global.kafka.consumer;

import com.project.Teaming.global.event.NotificationEvent;
import com.project.Teaming.global.sse.dto.EventPayload;
import com.project.Teaming.global.sse.dto.EventWithTeamPayload;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.entity.NotificationType;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import com.project.Teaming.global.sse.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationKafkaConsumer {

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;
    private final Executor notificationExecutor;

    @KafkaListener(topics = "notification-events", groupId = "notification-group", containerFactory = "kafkaListenerContainerFactory")
    public void consumeNotificationEvent(NotificationEvent event) {

        log.info("✅ 트랜잭션 종료 후 알림 전송 시작: {}",event.getNotificationIds());
        // ID 기반으로 Notification 객체 조회
        List<Notification> notifications = notificationRepository.findAllById(event.getNotificationIds());

        notifications.forEach(notification -> {
            if (shouldProcessMultiThreading(notification)) {
                CompletableFuture.runAsync(() -> sendWithMultiThreading(notification), notificationExecutor);
            } else {
                sendWithoutMultiThreading(notification);
            }
        });
        log.info("✅ 트랜잭션 종료 후 알림 전송 완료!");

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
