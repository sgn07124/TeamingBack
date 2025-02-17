package com.project.Teaming.global.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NotificationEvent {
    private final List<Long> notificationIds;
}
