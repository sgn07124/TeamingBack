package com.project.Teaming.global.sse.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationType {

    TEAM_JOIN_REQUEST("프로젝트"),
    PROJECT_TEAM_ACCEPT("프로젝트"),
    PROJECT_TEAM_REJECT("프로젝트"),
    PROJECT_TEAM_QUIT("프로젝트"),
    MENTORING_TEAM_JOIN_REQUEST("멘토링 팀 신청 알림"),
    MENTORING_TEAM_ACCEPT("멘토링 수락 알림"),
    MENTORING_TEAM_REJECT("멘토링 거절 알림"),
    MENTORING_EXPORT("멘토링 강퇴 알림"),
    MENTORING_EXPORT2("멘토링 강퇴된 유저에게 가는 알림"),
    MENTORING_DELETE("멘토링 탈퇴 알림"),
    WARNING_COUNT_INCREMENT("멘토링 경고횟수 증가 알림"),
    WELCOME_USER("공지");

    private String title;
}
