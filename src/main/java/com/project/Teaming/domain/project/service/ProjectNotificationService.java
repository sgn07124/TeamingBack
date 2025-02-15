package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectRole;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.global.annotation.NotifyAfterTransaction;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.entity.NotificationType;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import com.project.Teaming.global.sse.service.NotificationService;
import com.project.Teaming.global.sse.service.SseEmitterService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectNotificationService {

    private final ProjectParticipationRepository projectParticipationRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    /**
     * 팀에 참가 신청. 팀장에게 알림 전송
     */
    @NotifyAfterTransaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Long> participateTeam(ProjectTeam projectTeam, User user) {
        ProjectParticipation teamLeader = projectParticipationRepository.findByProjectTeamIdAndRole(projectTeam.getId(), ProjectRole.OWNER)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_OWNER));
        String message = user.getName() + " 님이 \"" + projectTeam.getName() + "\"팀에 참가 신청을 했습니다.";
        return sendSingleNotification(teamLeader.getUser().getId(), teamLeader.getProjectTeam().getId(), message, NotificationType.TEAM_JOIN_REQUEST);
    }

    // 한 명
    public List<Long> sendSingleNotification(Long userId, Long teamId, String message, NotificationType type) {
        Notification notification = (teamId == null)
                ? notificationService.saveNotification(userId, message, type.getTitle())
                : notificationService.saveNotificationWithTeamId(userId, teamId, message, type.getTitle());
        log.info("ProjectNotification Service sendSingleNotification 메서드 notification : {}", notification);
        return List.of(notification.getId());
    }

    // 여러 명
    public List<Long> sendBulkNotification(List<User> users, Long teamId, String message, NotificationType type) {
        List<Notification> notifications = users.stream()
                .map(user -> new Notification(user, message, teamId, type.getTitle()))
                .collect(Collectors.toList());

        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        return savedNotifications.stream().map(Notification::getId).collect(Collectors.toList());
    }
}
