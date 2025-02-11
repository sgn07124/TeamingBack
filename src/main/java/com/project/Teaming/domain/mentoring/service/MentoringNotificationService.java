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
import com.project.Teaming.global.sse.dto.EventPayload;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.entity.NotificationType;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import com.project.Teaming.global.sse.service.NotificationService;
import com.project.Teaming.global.sse.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MentoringNotificationService {

    private final NotificationService notificationService;
    private final UserDataProvider userDataProvider;
    private final MentoringTeamDataProvider mentoringTeamDataProvider;
    private final SseEmitterService sseEmitterService;
    private final MentoringParticipationDataProvider mentoringParticipationDataProvider;
    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void participate(Long userId, Long mentoringTeamId) {

        User user = userDataProvider.findUser(userId);
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        MentoringParticipation leader = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, null, MentoringAuthority.LEADER, MentoringParticipationStatus.ACCEPTED,
                () -> new BusinessException(ErrorCode.MENTORING_PARTICIPATION_NOT_EXIST));

        String message = user.getName() + " 님이 \"" + mentoringTeam.getName() + "\"팀에 참가 신청을 했습니다.";

        Notification notification = notificationService.saveNotification(leader.getUser().getId(), message, NotificationType.MENTORING_TEAM_JOIN_REQUEST.getTitle());

        try {
            sseEmitterService.send(leader.getUser().getId(),
                    EventPayload.builder()
                            .userId(notification.getUser().getId())
                            .type(notification.getType())
                            .createdAt(notification.getCreatedAt().toString())
                            .message(message)
                            .isRead(notification.isRead())
                            .build());
        } catch (Exception e) {
            System.out.println("❌ SSE 알림 전송 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public void accept(Long userId, Long mentoringTeamId) {

        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        String message = mentoringTeam.getName() + "\"팀의 신청이 수락되었습니다.";
        Notification notification = notificationService.saveNotification(userId, message, NotificationType.MENTORING_TEAM_ACCEPT.getTitle());
        sendNotification(notification);
    }

    @Transactional
    public void reject(Long userId, Long mentoringTeamId) {

        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        String message = mentoringTeam.getName() + "\"팀의 신청이 거절되었습니다.";
        Notification notification = notificationService.saveNotification(userId, message, NotificationType.MENTORING_TEAM_REJECT.getTitle());

        sendNotification(notification);
    }

    @Transactional
    public void warning(Long userId) {

        User user = userDataProvider.findUser(userId);

        String message = "경고 횟수가 증가하였습니다.";
        Notification notification = notificationService.saveNotification(userId, message, NotificationType.WARNING_COUNT_INCREMENT.getTitle());
        sendNotification(notification);
    }


    @Transactional
    public void export(Long userId, Long mentoringTeamId) {

        User user = userDataProvider.findUser(userId);
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        List<User> users = mentoringParticipationRepository.findMemberUser(mentoringTeam.getId(), MentoringParticipationStatus.ACCEPTED);
        String message = user.getName() + " 님이 \"" + mentoringTeam.getName() + "\"팀에서 강퇴 되었습니다. 신고는 7일 이내에 가능합니다.";

        List<Notification> notifications = new ArrayList<>();

        for (User u : users) {
            notifications.add(new Notification(u, message, NotificationType.MENTORING_EXPORT.getTitle()));
        }
        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);

        savedNotifications.forEach(this::sendNotification);
    }

    @Transactional
    public void delete(Long userId, Long mentoringTeamId) {

        User user = userDataProvider.findUser(userId);
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        List<User> users = mentoringParticipationRepository.findMemberUser(mentoringTeam.getId(), MentoringParticipationStatus.ACCEPTED);
        String message = user.getName() + " 님이 \"" + mentoringTeam.getName() + "\"팀에서 탈퇴 하였습니다. 신고는 7일 이내에 가능합니다.";

        List<Notification> notifications = new ArrayList<>();

        for (User u : users) {
            notifications.add(new Notification(u, message, NotificationType.MENTORING_DELETE.getTitle()));
        }
        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);

        savedNotifications.forEach(this::sendNotification);
    }

    private void sendNotification(Notification notification) {
        try {
            sseEmitterService.send(notification.getUser().getId(),
                    EventPayload.builder()
                            .userId(notification.getUser().getId())
                            .type(notification.getType())
                            .createdAt(notification.getCreatedAt().toString())
                            .message(notification.getMessage())
                            .isRead(notification.isRead())
                            .build());
        } catch (Exception e) {
            log.error("❌ SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }


}
