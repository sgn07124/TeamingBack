package com.project.Teaming.global.sse.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

    TEAM_JOIN_REQUEST("프로젝트");

    private String title;
}
