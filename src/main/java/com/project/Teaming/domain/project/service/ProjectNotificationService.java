package com.project.Teaming.domain.project.service;

import com.project.Teaming.domain.project.entity.ParticipationStatus;
import com.project.Teaming.domain.project.entity.ProjectParticipation;
import com.project.Teaming.domain.project.entity.ProjectRole;
import com.project.Teaming.domain.project.entity.ProjectTeam;
import com.project.Teaming.domain.project.repository.ProjectParticipationRepository;
import com.project.Teaming.domain.project.repository.ProjectTeamRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.global.annotation.NotifyAfterTransaction;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.entity.NotificationType;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import com.project.Teaming.global.sse.service.NotificationService;
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
    private final ProjectTeamRepository projectTeamRepository;
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

    @NotifyAfterTransaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Long> accept(ProjectParticipation joinMember) {
        String message = "\"" + joinMember.getProjectTeam().getName() + "\" 팀의 신청이 수락되었습니다.";
        return sendSingleNotification(joinMember.getUser().getId(), joinMember.getProjectTeam().getId(), message, NotificationType.PROJECT_TEAM_ACCEPT);
    }

    @NotifyAfterTransaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Long> reject(ProjectParticipation joinMember) {
        String message = "\"" + joinMember.getProjectTeam().getName() + "\" 팀의 신청이 거절되었습니다.";
        return sendSingleNotification(joinMember.getUser().getId(), joinMember.getProjectTeam().getId(), message, NotificationType.PROJECT_TEAM_REJECT);
    }

    public List<Long> quit(Long teamId, User quitUser) {
        List<User> users = projectParticipationRepository.findUsersByTeamIdAndStatus(teamId, ParticipationStatus.ACCEPTED, false, false);
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));
        String message = "\"" + quitUser.getName() + "\" 님이 " + "\"" + projectTeam.getName() + "\" 팀에서 탈퇴 하였습니다. 신고는 7일 이내에 가능합니다.";
        return sendBulkNotification(users, teamId, message, NotificationType.PROJECT_TEAM_QUIT);
    }

    public List<Long> export(Long teamId, User exportUser) {
        List<User> users = projectParticipationRepository.findUsersByTeamIdAndStatus(teamId, ParticipationStatus.ACCEPTED, false, false);
        ProjectTeam projectTeam = projectTeamRepository.findById(teamId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PROJECT_TEAM));
        String message = "\"" + exportUser.getName() + "\" 님이 " + "\"" + projectTeam.getName() + "\" 팀에서 강퇴 되었습니다. 신고는 7일 이내에 가능합니다.";
        return sendBulkNotification(users, teamId, message, NotificationType.PROJECT_TEAM_EXPORT);
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
