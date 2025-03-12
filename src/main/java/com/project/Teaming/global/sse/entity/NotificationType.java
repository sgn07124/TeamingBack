package com.project.Teaming.global.sse.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

    TEAM_JOIN_REQUEST("프로젝트","project"),
    PROJECT_TEAM_ACCEPT("프로젝트","project"),
    PROJECT_TEAM_REJECT("프로젝트","project"),
    PROJECT_TEAM_QUIT("프로젝트","project"),
    PROJECT_TEAM_EXPORT("프로젝트","project"),
    MENTORING_TEAM_JOIN_REQUEST("멘토링 팀 신청 알림","mentoring"),
    MENTORING_TEAM_ACCEPT("멘토링 수락 알림","mentoring"),
    MENTORING_TEAM_REJECT("멘토링 거절 알림","mentoring"),
    MENTORING_EXPORT("멘토링 강퇴 알림","mentoring"),
    MENTORING_EXPORT2("멘토링 강퇴된 유저에게 가는 알림","mentoring"),
    MENTORING_DELETE("멘토링 탈퇴 알림","mentoring"),
    WARNING_COUNT_INCREMENT("warning","전역"),
    WELCOME_USER("notice","전역"),
    WARNING("warning","전역");

    private String title;
    private String category;
}
