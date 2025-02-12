package com.project.Teaming.global.sse.service;

import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import com.project.Teaming.global.sse.dto.NotificationResponseDto;
import com.project.Teaming.global.sse.dto.NotificationWithTeamResponse;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final Map<Long, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * 알림 db에 저장
     * @param userId 알림 수신자 id
     * @param message
     * @param type
     * @return
     */
    @Transactional
    public Notification saveNotification(Long userId, String message, String type) {
        Notification notification = new Notification();
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification saveNotificationWithTeamId(Long userId, Long teamId, String message, String type) {
        Notification notification = new Notification();
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        notification.setUser(user);
        notification.setMessage(message);
        notification.setTeamId(teamId);
        notification.setType(type);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Transactional
    public List<NotificationResponseDto> getNotifications() {
        List<Notification> notifications =  notificationRepository.findByUserId(getCurrentId());
        return notifications.stream()
                .map(NotificationResponseDto::from).collect(Collectors.toList());
    }

    @Transactional
    public List<NotificationWithTeamResponse> getNotificationsWithTeam() {
        List<Notification> notifications =  notificationRepository.findByUserId(getCurrentId());
        return notifications.stream()
                .map(NotificationWithTeamResponse::from).collect(Collectors.toList());
    }

    private Long getCurrentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser.getUserId();
    }
}
