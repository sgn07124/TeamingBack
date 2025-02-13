package com.project.Teaming.domain.mentoring.aspect;

import com.project.Teaming.global.sse.dto.EventPayload;
import com.project.Teaming.global.sse.dto.EventWithTeamPayload;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.entity.NotificationType;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import com.project.Teaming.global.sse.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class NotificationAspect {

    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;
    private final Executor notificationExecutor;

    /**
     * 트랜잭션이 정상적으로 커밋된 후 알림을 전송하는 AOP
     * @param joinPoint - 실행된 메서드 정보
     * @param notificationIds - 트랜잭션에서 저장된 알림 ID 리스트
     */
    @AfterReturning(value = "@annotation(com.project.Teaming.domain.mentoring.annotation.NotifyAfterTransaction)", returning = "notificationIds")
    public void sendNotificationsAfterTransaction(JoinPoint joinPoint, List<Long> notificationIds) {
        log.info("✅ 트랜잭션 종료 후 알림 전송 시작: {}", notificationIds);

        // ID 기반으로 Notification 객체 조회
        List<Notification> notifications = notificationRepository.findAllById(notificationIds);

        notifications.forEach(notification -> {
            if (shouldProcessMultiThreading(notification)) {
                CompletableFuture.runAsync(() -> sendWithMultiThreading(notification), notificationExecutor);
            } else {
                sendWithoutMultiThreading(notification);
            }
        });
        log.info("✅ 트랜잭션 종료 후 알림 전송 완료!");
    }

    /**
     * ✅ 특정 알림 유형만 비동기 처리할지 여부를 판단하는 메서드
     */
    private boolean shouldProcessMultiThreading(Notification notification) {
        return
                (notification.getType().equals(NotificationType.MENTORING_EXPORT.getTitle())  || notification.getType().equals(NotificationType.MENTORING_DELETE.getTitle()));
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

