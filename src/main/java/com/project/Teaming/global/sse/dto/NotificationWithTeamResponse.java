package com.project.Teaming.global.sse.dto;

import com.project.Teaming.global.sse.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationWithTeamResponse {


    private Long notificationId;
    private Long userId;
    private String message;
    private String type;
    private Long teamId;
    private boolean isRead;
    private String createdAt;

    public static NotificationWithTeamResponse from(Notification notification) {
        NotificationWithTeamResponse dto = new NotificationWithTeamResponse();
        dto.setNotificationId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setTeamId(notification.getTeamId());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt().toString());
        return dto;
    }
}
