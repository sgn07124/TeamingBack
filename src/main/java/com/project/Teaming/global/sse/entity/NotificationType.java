package com.project.Teaming.global.sse.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

    TEAM_JOIN_REQUEST("프로젝트"),
    MENTORING_TEAM_JOIN_REQUEST("멘토링"),
    MENTORING_TEAM_ACCEPT("멘토링"),
    MENTORING_TEAM_REJECT("멘토링"),
    MENTORING_EXPORT("멘토링"),
    MENTORING_DELETE("멘토링"),
    WARNING_COUNT_INCREMENT("멘토링");

    private String title;
}
