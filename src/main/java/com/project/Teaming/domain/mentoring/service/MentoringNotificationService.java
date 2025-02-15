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
import com.project.Teaming.global.annotation.NotifyAfterTransaction;
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
import org.springframework.transaction.annotation.Propagation;
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
    private final MentoringParticipationDataProvider mentoringParticipationDataProvider;
    private final MentoringParticipationRepository mentoringParticipationRepository;
    private final NotificationRepository notificationRepository;
    private final SseEmitterService sseEmitterService;

    @NotifyAfterTransaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Long> participate(Long userId, Long mentoringTeamId) {

        User user = userDataProvider.findUser(userId);
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        MentoringParticipation leader = mentoringParticipationDataProvider.findParticipationWith(
                mentoringTeam, null, MentoringAuthority.LEADER, MentoringParticipationStatus.ACCEPTED,
                () -> new BusinessException(ErrorCode.MENTORING_PARTICIPATION_NOT_EXIST));

        String message = user.getName() + " 님이 " + mentoringTeam.getName() + " 팀에 참가 신청을 했습니다.";
        return sendSingleNotification(leader.getUser().getId(), mentoringTeamId, message, NotificationType.MENTORING_TEAM_JOIN_REQUEST);
    }

    public List<Long> accept(Long userId, Long mentoringTeamId) {

        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        String message = mentoringTeam.getName() + " 팀의 신청이 수락되었습니다.";
        return sendSingleNotification(userId, mentoringTeamId, message, NotificationType.MENTORING_TEAM_ACCEPT);
    }

    public List<Long> reject(Long userId, Long mentoringTeamId) {

        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        String message = mentoringTeam.getName() + " 팀의 신청이 거절되었습니다.";
        return sendSingleNotification(userId, mentoringTeamId, message, NotificationType.MENTORING_TEAM_REJECT);
    }

    @NotifyAfterTransaction
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<Long> warning(Long userId) {
        String message = "경고 횟수가 증가하였습니다.";
        log.info("MentoringNotification Service Warning 메서드");
        return sendSingleNotification(userId, null, message, NotificationType.WARNING_COUNT_INCREMENT);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyExportedUser(Long userId, Long mentoringTeamId) {

        User user = userDataProvider.findUser(userId);
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        String message =mentoringTeam.getName() + " 팀에서 강퇴되었습니다.";

        Notification notification = new Notification(user, message, mentoringTeam.getId(), NotificationType.MENTORING_EXPORT2.getTitle());
        notificationRepository.save(notification);

        try {
            sseEmitterService.send(user.getId(),
                    EventPayload.builder()
                            .userId(notification.getUser().getId())
                            .type(notification.getType())
                            .createdAt(notification.getCreatedAt().toString())
                            .message(message)
                            .isRead(false)
                            .build());
        } catch (Exception e) {
            log.error("❌ 강퇴된 사용자 SSE 알림 전송 실패: {}", e.getMessage(), e);
        }
    }


    public List<Long> export(Long userId, Long mentoringTeamId) {

        User user = userDataProvider.findUser(userId);
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        List<User> users = mentoringParticipationRepository.findMemberUser(mentoringTeam.getId(), MentoringAuthority.CREW);
        String message = user.getName() + " 님이 " + mentoringTeam.getName() + " 팀에서 강퇴 되었습니다. 신고는 7일 이내에 가능합니다.";
        return sendBulkNotification(users, mentoringTeamId, message, NotificationType.MENTORING_EXPORT);
    }


    public List<Long> delete(Long userId, Long mentoringTeamId) {

        User user = userDataProvider.findUser(userId);
        MentoringTeam mentoringTeam = mentoringTeamDataProvider.findMentoringTeam(mentoringTeamId);

        List<User> users = mentoringParticipationRepository.findMemberUser(mentoringTeam.getId(), null);
        String message = user.getName() + " 님이 " + mentoringTeam.getName() + " 팀에서 탈퇴 하였습니다. 신고는 7일 이내에 가능합니다.";

        return sendBulkNotification(users, mentoringTeamId, message, NotificationType.MENTORING_DELETE);
    }

    public List<Long> sendSingleNotification(Long userId, Long teamId, String message, NotificationType type) {
        Notification notification = (teamId == null)
                ? notificationService.saveNotification(userId, message, type.getTitle())
                : notificationService.saveNotificationWithTeamId(userId, teamId, message, type.getTitle());
        log.info("MentoringNotification Service sendSingleNotification 메서드 notification : {}", notification);
        return List.of(notification.getId());
    }

    public List<Long> sendBulkNotification(List<User> users, Long teamId, String message, NotificationType type) {
        List<Notification> notifications = users.stream()
                .map(user -> new Notification(user, message, teamId, type.getTitle()))
                .collect(Collectors.toList());

        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        return savedNotifications.stream().map(Notification::getId).collect(Collectors.toList());
    }

}
