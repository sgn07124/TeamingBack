package com.project.Teaming.global.sse.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.Teaming.global.sse.entity.Notification;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long notificationId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    private String message;
    private String type;
    private String category;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long teamId;
    private boolean isRead;
    private String createdAt;

    public static NotificationResponseDto from(Notification notification) {
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setNotificationId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setCategory(notification.getCategory());
        dto.setTeamId(notification.getTeamId());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(dto.getFormattedDate(notification.getCreatedAt()));
        return dto;
    }

    public String getFormattedDate(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }
}
