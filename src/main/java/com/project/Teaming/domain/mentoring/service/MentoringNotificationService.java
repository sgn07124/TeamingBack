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
import com.project.Teaming.global.sse.dto.EventWithTeamPayload;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.entity.NotificationType;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import com.project.Teaming.global.sse.service.NotificationService;
import com.project.Teaming.global.sse.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
        sendSingleNotification(leader.getUser().getId(), mentoringTeamId, message, NotificationType.MENTORING_TEAM_JOIN_REQUEST);
    }

    @Transactional
    public void accept(Long userId, Long mentoringTeamId) {

        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        String message = mentoringTeam.getName() + "\"팀의 신청이 수락되었습니다.";
        sendSingleNotification(userId, mentoringTeamId, message, NotificationType.MENTORING_TEAM_ACCEPT);
    }

    @Transactional
    public void reject(Long userId, Long mentoringTeamId) {

        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        String message = mentoringTeam.getName() + "\"팀의 신청이 거절되었습니다.";
        sendSingleNotification(userId, mentoringTeamId, message, NotificationType.MENTORING_TEAM_REJECT);
    }

    @Transactional
    public void warning(Long userId) {

        User user = userDataProvider.findUser(userId);

        String message = "경고 횟수가 증가하였습니다.";
        sendSingleNotification(userId, null, message, NotificationType.WARNING_COUNT_INCREMENT);
    }


    @Transactional
    public void export(Long userId, Long mentoringTeamId) {

        User user = userDataProvider.findUser(userId);
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        List<User> users = mentoringParticipationRepository.findMemberUser(mentoringTeam.getId(), MentoringParticipationStatus.ACCEPTED);
        String message = user.getName() + " 님이 \"" + mentoringTeam.getName() + "\"팀에서 강퇴 되었습니다. 신고는 7일 이내에 가능합니다.";

        sendBulkNotification(users, mentoringTeamId, message, NotificationType.MENTORING_EXPORT);
    }

    @Transactional
    public void delete(Long userId, Long mentoringTeamId) {

        User user = userDataProvider.findUser(userId);
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        List<User> users = mentoringParticipationRepository.findMemberUser(mentoringTeam.getId(), MentoringParticipationStatus.ACCEPTED);
        String message = user.getName() + " 님이 \"" + mentoringTeam.getName() + "\"팀에서 탈퇴 하였습니다. 신고는 7일 이내에 가능합니다.";

        sendBulkNotification(users, mentoringTeamId, message, NotificationType.MENTORING_DELETE);
    }

    @Transactional
    public void sendSingleNotification(Long userId, Long teamId, String message, NotificationType type) {
        Notification notification = (teamId == null)
                ? notificationService.saveNotification(userId, message, type.getTitle())
                : notificationService.saveNotificationWithTeamId(userId, teamId, message, type.getTitle());

        sendToClient(notification);
    }

    @Transactional
    public void sendBulkNotification(List<User> users, Long teamId, String message, NotificationType type) {
        List<Notification> notifications = users.stream()
                .map(user -> new Notification(user, message, teamId, type.getTitle()))
                .collect(Collectors.toList());

        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        savedNotifications.forEach(this::sendToClient);
    }


    private void sendToClient(Notification notification) {
        try {
            if (notification.getTeamId() != null) {
                sseEmitterService.sendWithTeamId(notification.getUser().getId(),
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
            log.error("❌ SSE 알림 전송 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
