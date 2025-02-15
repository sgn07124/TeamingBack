package com.project.Teaming.domain.user.service;

import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.global.annotation.NotifyAfterTransaction;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.entity.NotificationType;
import com.project.Teaming.global.sse.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationService {

    private final NotificationService notificationService;

    @NotifyAfterTransaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Long> join(User user) {
        String message = user.getName() + "님, 환영합니다! 프로젝트 & 멘토링 매칭을 지원하는 Teaming 입니다.";
        return sendSingleNotification(user.getId(), null, message, NotificationType.WELCOME_USER);
    }

    public List<Long> sendSingleNotification(Long userId, Long teamId, String message, NotificationType type) {
        Notification notification = (teamId == null)
                ? notificationService.saveNotification(userId, message, type.getTitle())
                : notificationService.saveNotificationWithTeamId(userId, teamId, message, type.getTitle());
        log.info("MentoringNotification Service sendSingleNotification 메서드 notification : {}", notification);
        return List.of(notification.getId());
    }
}
