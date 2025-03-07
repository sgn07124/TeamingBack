package com.project.Teaming.global.sse.service;

import com.project.Teaming.domain.user.entity.User;
import com.project.Teaming.domain.user.repository.UserRepository;
import com.project.Teaming.global.error.ErrorCode;
import com.project.Teaming.global.error.exception.BusinessException;
import com.project.Teaming.global.jwt.dto.SecurityUserDto;
import com.project.Teaming.global.sse.dto.NotificationRequestDto;
import com.project.Teaming.global.sse.dto.NotificationResponseDto;
import com.project.Teaming.global.sse.entity.Notification;
import com.project.Teaming.global.sse.repository.NotificationRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
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
    public Notification saveNotification(Long userId, String message, String type, String category) {
        Notification notification = new Notification();
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setCategory(category);
        notification.setTeamId(null);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification saveNotificationWithTeamId(Long userId, Long teamId, String message, String type, String category) {
        Notification notification = new Notification();
        User user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_EXIST));
        notification.setUser(user);
        notification.setMessage(message);
        notification.setTeamId(teamId);
        notification.setType(type);
        notification.setCategory(category);
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    @Transactional
    public List<NotificationResponseDto> getNotifications() {
        List<Notification> notifications =  notificationRepository.findByUserId(getCurrentId(), Sort.by(Sort.Order.desc("createdAt")));
        return notifications.stream()
                .map(NotificationResponseDto::from).collect(Collectors.toList());
    }

    public void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NOTIFICATION));
        notificationRepository.delete(notification);
    }

    private Long getCurrentId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUserDto securityUser = (SecurityUserDto) authentication.getPrincipal();
        return securityUser.getUserId();
    }

    @Transactional
    public int markAsRead(NotificationRequestDto dto) {
        List<Long> ids = getLongIds(dto);
        return notificationRepository.markNotificationsAsRead(ids);
    }

    @Transactional
    public int deleteNotifications(NotificationRequestDto dto) {
        List<Long> ids = getLongIds(dto);
        return notificationRepository.deleteNotificationsByIds(ids);
    }

    private List<Long> getLongIds(NotificationRequestDto dto) {
        List<Long> ids = dto.getNotificationIds().stream()
                .map(this::convertToLong)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (ids.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_VALID_IDS);
        }
        return ids;
    }

    private Long convertToLong(String id) {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null; // 변환 실패 시 null 반환
        }
    }
}
