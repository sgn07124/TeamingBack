package com.project.Teaming.domain.mentoring.service;

import com.project.Teaming.domain.mentoring.entity.MentoringAuthority;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipation;
import com.project.Teaming.domain.mentoring.entity.MentoringParticipationStatus;
import com.project.Teaming.domain.mentoring.entity.MentoringTeam;
import com.project.Teaming.domain.mentoring.provider.MentoringParticipationDataProvider;
import com.project.Teaming.domain.mentoring.provider.MentoringTeamDataProvider;
import com.project.Teaming.domain.mentoring.provider.UserDataProvider;
import com.project.Teaming.domain.mentoring.repository.MentoringParticipationRepository;
import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.event.NotificationEvent;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.entity.NotificationType;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import com.project.Teaming.global.sse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentoringNotificationService {

    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserDataProvider userDataProvider;
    private final MentoringTeamDataProvider mentoringTeamDataProvider;
    private final MentoringParticipationDataProvider mentoringParticipationDataProvider;
    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final NotificationRepository notificationRepository;

    public void participate(Long userId, Long mentoringTeamId) {

        User user = userDataProvider.findUser(userId);
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        MentoringParticipation leader = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, null, MentoringAuthority.LEADER, MentoringParticipationStatus.ACCEPTED,
                () -> new BusinessException(ErrorCode.MENTORING_PARTICIPATION_NOT_EXIST));

        String message = user.getName() + " 님이 " + mentoringTeam.getName() + " 팀에 참가 신청을 했습니다.";
        sendSingleNotification(leader.getUser().getId(), mentoringTeamId, message, NotificationType.MENTORING_TEAM_JOIN_REQUEST);
    }

    public void accept(Long userId, Long mentoringTeamId) {

        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        String message = mentoringTeam.getName() + " 팀의 신청이 수락되었습니다.";
        sendSingleNotification(userId, mentoringTeamId, message, NotificationType.MENTORING_TEAM_ACCEPT);
    }

    public void reject(Long userId, Long mentoringTeamId) {

        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        String message = mentoringTeam.getName() + " 팀의 신청이 거절되었습니다.";
        sendSingleNotification(userId, mentoringTeamId, message, NotificationType.MENTORING_TEAM_REJECT);
    }


    public void warning(Long userId) {
        String message = "경고 횟수가 증가하였습니다.";
        log.info("MentoringNotification Service Warning 메서드");
        sendSingleNotification(userId, null, message, NotificationType.WARNING_COUNT_INCREMENT);
    }


    public void notifyExportedUser(Long userId, Long mentoringTeamId) {
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        String message =mentoringTeam.getName() + " 팀에서 강퇴되었습니다.";

        sendSingleNotification(userId, mentoringTeamId, message, NotificationType.MENTORING_EXPORT2);
    }


    public void export(Long userId, Long mentoringTeamId) {

        User user = userDataProvider.findUser(userId);
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        List<User> users = mentoringParticipationRepository.findMemberUser(mentoringTeam.getId(), MentoringAuthority.CREW);
        String message = user.getName() + " 님이 " + mentoringTeam.getName() + " 팀에서 강퇴 되었습니다. 신고는 7일 이내에 가능합니다.";
        sendBulkNotification(users, mentoringTeamId, message, NotificationType.MENTORING_EXPORT);
    }


    public void delete(Long userId, Long mentoringTeamId) {

        User user = userDataProvider.findUser(userId);
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        List<User> users = mentoringParticipationRepository.findMemberUser(mentoringTeam.getId(), null);
        String message = user.getName() + " 님이 " + mentoringTeam.getName() + " 팀에서 탈퇴 하였습니다. 신고는 7일 이내에 가능합니다.";

        sendBulkNotification(users, mentoringTeamId, message, NotificationType.MENTORING_DELETE);
    }

    public void sendSingleNotification(Long userId, Long teamId, String message, NotificationType type) {
        Notification notification = (teamId == null)
                ? notificationService.saveNotification(userId, message, type.getTitle(),type.getCategory())
                : notificationService.saveNotificationWithTeamId(userId, teamId, message, type.getTitle(),type.getCategory());
        log.info("MentoringNotification Service sendSingleNotification 메서드 notification : {}", notification);
        eventPublisher.publishEvent(new NotificationEvent(List.of(notification.getId()))); ;
    }

    public void sendBulkNotification(List<User> users, Long teamId, String message, NotificationType type) {
        List<Notification> notifications = users.stream()
                .map(user -> new Notification(user, message, teamId, type.getTitle(), type.getCategory()))
                .collect(Collectors.toList());

        List<Long> notificationIds = notificationRepository.saveAll(notifications).stream()
                .map(Notification::getId).collect(Collectors.toList());
        eventPublisher.publishEvent(new NotificationEvent(notificationIds));
    }

}
