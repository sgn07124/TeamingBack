package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.entity.ParticipationStatus;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectRole;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.project.repository.ProjectTeamRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.event.NotificationEvent;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.entity.NotificationType;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import com.project.Teaming.global.sse.service.NotificationService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectNotificationService {

    private final ProjectParticipationRepository projectParticipationRepository;
    private final NotificationRepository notificationRepository;
    private final ProjectTeamRepository projectTeamRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    public void participateTeam(ProjectTeam projectTeam, User user) {
        ProjectParticipation teamLeader = projectParticipationRepository.findByProjectTeamIdAndRole(projectTeam.getId(), ProjectRole.OWNER)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_OWNER));
        String message = user.getName() + " 님이 \"" + projectTeam.getName() + "\"팀에 참가 신청을 했습니다.";
        sendSingleNotification(teamLeader.getUser().getId(), teamLeader.getProjectTeam().getId(), message, NotificationType.TEAM_JOIN_REQUEST);
    }

    public void accept(ProjectParticipation joinMember) {
        String message = "\"" + joinMember.getProjectTeam().getName() + "\" 팀의 신청이 수락되었습니다.";
        sendSingleNotification(joinMember.getUser().getId(), joinMember.getProjectTeam().getId(), message, NotificationType.PROJECT_TEAM_ACCEPT);
    }

    public void reject(ProjectParticipation joinMember) {
        String message = "\"" + joinMember.getProjectTeam().getName() + "\" 팀의 신청이 거절되었습니다.";
        sendSingleNotification(joinMember.getUser().getId(), joinMember.getProjectTeam().getId(), message, NotificationType.PROJECT_TEAM_REJECT);
    }

    public void quit(Long teamId, User quitUser) {
        List<User> users = projectParticipationRepository.findUsersByTeamIdAndStatus(teamId, ParticipationStatus.ACCEPTED, false, false);
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));
        String message = "\"" + quitUser.getName() + "\" 님이 " + "\"" + projectTeam.getName() + "\" 팀에서 탈퇴 하였습니다. 신고는 7일 이내에 가능합니다.";
        sendBulkNotification(users, teamId, message, NotificationType.PROJECT_TEAM_QUIT);
    }

    public void export(Long teamId, User exportUser) {
        List<User> users = projectParticipationRepository.findUsersByTeamIdAndStatus(teamId, ParticipationStatus.ACCEPTED, false, false);
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));
        String message = "\"" + exportUser.getName() + "\" 님이 " + "\"" + projectTeam.getName() + "\" 팀에서 강퇴 되었습니다. 신고는 7일 이내에 가능합니다.";
        sendBulkNotification(users, teamId, message, NotificationType.PROJECT_TEAM_EXPORT);
    }

    public void warning(User reportedUser) {
        String message = "경고 횟수가 " + reportedUser.getWarningCount() + "로 증가했습니다.";
        sendSingleNotification(reportedUser.getId(), null, message, NotificationType.WARNING);
    }

    // 한 명
    public void sendSingleNotification(Long userId, Long teamId, String message, NotificationType type) {
        Notification notification = (teamId == null)
                ? notificationService.saveNotification(userId, message, type.getTitle(), type.getCategory())
                : notificationService.saveNotificationWithTeamId(userId, teamId, message, type.getTitle(), type.getCategory());
        log.info("ProjectNotification Service sendSingleNotification 메서드 notification : {}", notification);
        eventPublisher.publishEvent(new NotificationEvent(List.of(notification.getId())));
    }

    // 여러 명
    public void sendBulkNotification(List<User> users, Long teamId, String message, NotificationType type) {
        List<Notification> notifications = users.stream()
                .map(user -> new Notification(user, message, teamId, type.getTitle(),type.getCategory()))
                .collect(Collectors.toList());

        List<Long> notificationIds = notificationRepository.saveAll(notifications).stream()
                .map(Notification::getId).collect(Collectors.toList());
        eventPublisher.publishEvent(new NotificationEvent(notificationIds));
    }
}
